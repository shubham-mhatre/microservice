
package com.sm.order_service.exception;

import java.time.LocalDateTime;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sm.order_service.dto.response.ExceptionResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ExceptionResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex,
			HttpServletRequest request) {

		log.warn("Resource not found | URI: {} | Message: {}", request.getRequestURI(), ex.getMessage());

		return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler(InvalidRequestStateException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidRequestStateException(InvalidRequestStateException ex,
			HttpServletRequest request) {

		log.warn("Invalid request state | URI: {} | Message: {}", request.getRequestURI(), ex.getMessage());

		return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler(ServiceDownException.class)
	public ResponseEntity<ExceptionResponseDto> handleServiceDownException(ServiceDownException ex,
			HttpServletRequest request) {

		log.error("Downstream service failure | URI: {}", request.getRequestURI(), ex);

		return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request);
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ResponseEntity<ExceptionResponseDto> handleOptimisticLockingFailure(OptimisticLockingFailureException ex,
			HttpServletRequest request) {

		log.warn("Concurrent order modification detected | URI: {} | Message: {}", request.getRequestURI(),
				ex.getMessage());

		return buildResponse("Order was modified by another request. Please retry.", HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler(CannotAcquireLockException.class)
	public ResponseEntity<ExceptionResponseDto> handleLockConflict(CannotAcquireLockException ex,
			HttpServletRequest request) {

		log.warn("Unable to acquire database lock | URI: {} | Message: {}", request.getRequestURI(), ex.getMessage());

		return buildResponse("This order is being updated by another request. Please retry.", HttpStatus.CONFLICT,
				request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {

		log.error("Unexpected application error | URI: {}", request.getRequestURI(), ex);

		return buildResponse("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	private ResponseEntity<ExceptionResponseDto> buildResponse(String message, HttpStatus status,
			HttpServletRequest request) {

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), message, status.value(),
				status.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(status).body(response);
	}
}
