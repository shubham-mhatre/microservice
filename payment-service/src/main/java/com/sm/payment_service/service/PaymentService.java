package com.sm.payment_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sm.payment_service.client.OrderServiceClient;
import com.sm.payment_service.dto.request.PaymentRequest;
import com.sm.payment_service.dto.request.ProcessPaymentRequest;
import com.sm.payment_service.dto.response.OrderDetailsResponse;
import com.sm.payment_service.dto.response.PageResponse;
import com.sm.payment_service.dto.response.PaymentResponse;
import com.sm.payment_service.dto.respositories.PaymentRepository;
import com.sm.payment_service.entity.Payment;
import com.sm.payment_service.enums.OrderSyncStatus;
import com.sm.payment_service.enums.PaymentStatus;
import com.sm.payment_service.exception.InvalidPaymentException;
import com.sm.payment_service.exception.PaymentConflictException;
import com.sm.payment_service.exception.PaymentNotFoundException;
import com.sm.payment_service.mapper.PaymentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderServiceClient orderServiceClient;
	private final PaymentMapper paymentMapper;

	private static final int MAX_SYNC_ATTEMPTS = 5;

	@Transactional
	public PaymentResponse initiatePayment(PaymentRequest request) {

		// Retrieve the order through Order Service, not its database.
		OrderDetailsResponse order = orderServiceClient.getOrderById(request.getOrderId());

		// Only pending orders can start a payment attempt.
		if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
			throw new InvalidPaymentException("Payment can only be initiated for a PENDING order");
		}

		BigDecimal amount = order.getTotalAmount();

		if (amount == null || amount.signum() <= 0) {
			throw new InvalidPaymentException("Order total amount must be greater than zero");
		}

		// Block an existing initiated or successful payment.
		boolean existingPayment = paymentRepository.existsByOrderIdAndStatusIn(request.getOrderId(),
				EnumSet.of(PaymentStatus.INITIATED, PaymentStatus.SUCCESS));

		if (existingPayment) {
			throw new PaymentConflictException("An initiated or successful payment already exists for this order");
		}

		Payment payment = new Payment();
		payment.setOrderId(order.getId());
		payment.setAmount(amount);
		payment.setCurrency("INR");
		payment.setPaymentMethod(request.getPaymentMethod());
		payment.setStatus(PaymentStatus.INITIATED);
		payment.setTransactionReference("PAY-" + UUID.randomUUID());

		Payment savedPayment = paymentRepository.saveAndFlush(payment);

		return paymentMapper.mapPaymentToResponse(savedPayment);
	}

	/**
	 * Retrieves payment details using the payment ID.
	 *
	 * @param paymentId the ID of the payment to retrieve
	 * @return the payment details
	 * @throws PaymentNotFoundException if the payment does not exist
	 */
	public PaymentResponse getPaymentById(Long paymentId) {

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

		return paymentMapper.mapPaymentToResponse(payment);
	}

	/**
	 * Retrieves all payment attempts associated with an order.
	 *
	 * @param orderId the ID of the order
	 * @return payment attempts sorted by creation time, newest first
	 * @throws PaymentNotFoundException if no payments exist for the order
	 */
	public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {

		List<Payment> payments = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);

		if (payments.isEmpty()) {
			throw new PaymentNotFoundException("No payments found for order ID: " + orderId);
		}

		return payments.stream().map(paymentMapper::mapPaymentToResponse).toList();
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public PaymentResponse processPayment(Long paymentId, ProcessPaymentRequest request) {

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

		PaymentStatus requestedStatus = PaymentStatus.valueOf(request.status());

		if (payment.getStatus() != PaymentStatus.INITIATED) {
			throw new PaymentConflictException(
					"Payment cannot be processed because its current status is " + payment.getStatus());
		}

		payment.setStatus(requestedStatus);

		if (requestedStatus == PaymentStatus.FAILED) {
			payment.setFailureReason("Payment failed during processing");
		} else {
			payment.setFailureReason(null);
		}

		Payment savedPayment = paymentRepository.saveAndFlush(payment);

		// Synchronize Order Service for both SUCCESS and FAILED payments
		return synchronizeOrder(savedPayment.getId());
	}

	public PageResponse<PaymentResponse> getAllPayments(int page, int size, String sortBy, String direction) {

		if (page < 0) {
			throw new InvalidPaymentException("Page number cannot be negative");
		}

		if (size <= 0) {
			throw new InvalidPaymentException("Page size must be greater than zero");
		}

		Sort.Direction sortDirection;

		try {
			sortDirection = Sort.Direction.fromString(direction);
		} catch (IllegalArgumentException ex) {
			throw new InvalidPaymentException("Direction must be either 'asc' or 'desc'");
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

		Page<Payment> paymentPage = paymentRepository.findAll(pageable);

		List<PaymentResponse> items = paymentPage.getContent().stream().map(paymentMapper::mapPaymentToResponse)
				.toList();

		return new PageResponse<>(items, paymentPage.getNumber(), paymentPage.getSize(), paymentPage.getTotalElements(),
				paymentPage.getTotalPages(), paymentPage.hasNext(), paymentPage.hasPrevious());
	}

	public PaymentResponse synchronizeOrder(Long paymentId) {

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

		if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.FAILED) {

			throw new InvalidPaymentException(
					"Order synchronization is only allowed for successful or failed payments.");
		}

		if (payment.getOrderSyncStatus() == OrderSyncStatus.SUCCESS) {
			return paymentMapper.mapPaymentToResponse(payment);
		}

		if (payment.getOrderSyncAttempts() >= MAX_SYNC_ATTEMPTS) {
			payment.setOrderSyncStatus(OrderSyncStatus.FAILED);

			Payment updatedPayment = paymentRepository.saveAndFlush(payment);

			return paymentMapper.mapPaymentToResponse(updatedPayment);
		}

		// Increment attempt count and persist before calling Order Service.
		payment.setOrderSyncAttempts(payment.getOrderSyncAttempts() + 1);
		payment.setOrderSyncError(null);

		// IMPORTANT: Use the returned entity because @Version may change.
		payment = paymentRepository.saveAndFlush(payment);

		String orderStatus = payment.getStatus() == PaymentStatus.SUCCESS ? "CONFIRMED" : "FAILED";

		try {

			orderServiceClient.updateOrderStatus(payment.getOrderId(), orderStatus);

			payment.setOrderSyncStatus(OrderSyncStatus.SUCCESS);
			payment.setOrderSyncedAt(LocalDateTime.now());
			payment.setOrderSyncError(null);

		} catch (RuntimeException ex) {

			payment.setOrderSyncError(ex.getMessage());

			if (payment.getOrderSyncAttempts() >= MAX_SYNC_ATTEMPTS) {
				payment.setOrderSyncStatus(OrderSyncStatus.FAILED);
			} else {
				payment.setOrderSyncStatus(OrderSyncStatus.PENDING);
			}
		}

		// Save the updated entity, not the original detached instance.
		Payment updatedPayment = paymentRepository.saveAndFlush(payment);

		return paymentMapper.mapPaymentToResponse(updatedPayment);
	}

	// manual retry for admin
	public PaymentResponse retryOrderSynchronization(Long paymentId) {

		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

		if (payment.getStatus() != PaymentStatus.SUCCESS) {
			throw new InvalidPaymentException("Only successful payments can be synchronized with orders.");
		}

		if (payment.getOrderSyncStatus() == OrderSyncStatus.SUCCESS) {
			return paymentMapper.mapPaymentToResponse(payment);
		}

		// Allow a fresh retry cycle after automatic attempts are exhausted.
		if (payment.getOrderSyncStatus() == OrderSyncStatus.FAILED) {
			payment.setOrderSyncAttempts(0);
			payment.setOrderSyncStatus(OrderSyncStatus.PENDING);
			payment.setOrderSyncError(null);
			paymentRepository.save(payment);
		}

		return synchronizeOrder(paymentId);
	}

}