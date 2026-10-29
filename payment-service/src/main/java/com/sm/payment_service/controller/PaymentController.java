package com.sm.payment_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.payment_service.dto.request.PaymentRequest;
import com.sm.payment_service.dto.request.ProcessPaymentRequest;
import com.sm.payment_service.dto.response.ApiResponse;
import com.sm.payment_service.dto.response.PageResponse;
import com.sm.payment_service.dto.response.PaymentResponse;
import com.sm.payment_service.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentRequest request) {

		PaymentResponse response = paymentService.initiatePayment(request);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Payment initiated successfully", response));
	}

	/**
	 * Retrieves payment details by payment ID.
	 *
	 * @param paymentId the ID of the payment
	 * @return the payment details wrapped in ApiResponse
	 */
	@GetMapping("/{paymentId}")
	public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long paymentId) {

		PaymentResponse response = paymentService.getPaymentById(paymentId);

		return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", response));
	}

	/**
	 * Retrieves all payment attempts for an order.
	 *
	 * @param orderId the ID of the order
	 * @return payment attempts wrapped in ApiResponse
	 */
	@GetMapping("/order/{orderId}")
	public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrderId(@PathVariable Long orderId) {

		List<PaymentResponse> response = paymentService.getPaymentsByOrderId(orderId);

		return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", response));
	}

	@PatchMapping("/{paymentId}")
	public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@PathVariable Long paymentId,
			@Valid @RequestBody ProcessPaymentRequest request) {

		PaymentResponse response = paymentService.processPayment(paymentId, request);

		return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		PageResponse<PaymentResponse> response = paymentService.getAllPayments(page, size, sortBy, direction);

		return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
	}

}