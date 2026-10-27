package com.sm.payment_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sm.payment_service.dto.request.PaymentRequest;
import com.sm.payment_service.dto.response.ApiResponse;
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
}