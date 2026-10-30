package com.sm.payment_service.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sm.payment_service.dto.response.ExceptionResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(PaymentNotFoundException.class)
	public ResponseEntity<ExceptionResponseDto> handlePaymentNotFound(PaymentNotFoundException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ExceptionResponseDto> handleOrderNotFound(OrderNotFoundException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidPaymentException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidPayment(InvalidPaymentException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	@ExceptionHandler(OrderServiceUnavailableException.class)
	public ResponseEntity<ExceptionResponseDto> handleOrderServiceUnavailable(OrderServiceUnavailableException ex,
			HttpServletRequest request) {

		log.error("Order Service communication failure", ex);

		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
				"Order Service is currently unavailable. Please try again later.", request);
	}
	
	@ExceptionHandler(InvalidRequestStateException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidRequestStateException(InvalidRequestStateException ex,
			HttpServletRequest request) {

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), ex.getMessage(),
				HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponseDto> handleValidationErrors(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).distinct()
				.collect(Collectors.joining(", "));

		return buildResponse(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponseDto> handleConstraintViolations(ConstraintViolationException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidRequestBody(HttpMessageNotReadableException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request body or unsupported enum value.", request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ExceptionResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex,
			HttpServletRequest request) {

		log.warn("Payment data integrity violation: {}", ex.getMessage());

		return buildResponse(HttpStatus.CONFLICT, "The payment request conflicts with existing data.", request);
	}

	@ExceptionHandler(PaymentConflictException.class)
	public ResponseEntity<ExceptionResponseDto> handlePaymentConflict(PaymentConflictException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleUnexpectedException(Exception ex, HttpServletRequest request) {

		log.error("Unexpected Payment Service error", ex);

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
	}

	private ResponseEntity<ExceptionResponseDto> buildResponse(HttpStatus status, String message,
			HttpServletRequest request) {

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), message, status.value(),
				status.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(status).body(response);
	}
}