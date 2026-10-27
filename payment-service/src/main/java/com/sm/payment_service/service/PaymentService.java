package com.sm.payment_service.service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.payment_service.client.OrderServiceClient;
import com.sm.payment_service.dto.request.PaymentRequest;
import com.sm.payment_service.dto.response.OrderDetailsResponse;
import com.sm.payment_service.dto.response.PaymentResponse;
import com.sm.payment_service.dto.respositories.PaymentRepository;
import com.sm.payment_service.entity.Payment;
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

		Payment savedPayment = paymentRepository.save(payment);

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

}