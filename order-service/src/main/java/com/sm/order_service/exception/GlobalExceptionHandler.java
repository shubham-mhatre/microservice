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

		log.error("ResourceNotFoundException on {}", request.getRequestURI(), ex);

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), ex.getMessage(),
				HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidRequestStateException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidRequestStateException(InvalidRequestStateException ex,
			HttpServletRequest request) {

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), ex.getMessage(),
				HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(ServiceDownException.class)
	public ResponseEntity<ExceptionResponseDto> handleServiceDownException(ServiceDownException ex,
			HttpServletRequest request) {

		log.error("Downstream service failure on {}", request.getRequestURI(), ex);

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), ex.getMessage(),
				HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
				request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ResponseEntity<ExceptionResponseDto> handleOptimisticLockingFailure(OptimisticLockingFailureException ex,
			HttpServletRequest request) {

		log.info("inside OptimisticLockingFailureException handler : Concurrent order modification detected on {}", request.getRequestURI(), ex);

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(),
				"Order was modified by another request. Please retry.", HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(CannotAcquireLockException.class)
	public ResponseEntity<ExceptionResponseDto> handleLockConflict(CannotAcquireLockException ex,
			HttpServletRequest request) {

		log.info("inside CannotAcquireLockException handler : order update detected for {}: {}", request.getRequestURI(), ex.getMessage());

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(),
				"This order is being updated by another request. Please retry.", HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(), request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {

		log.error("Unexpected error on {}", request.getRequestURI(), ex);

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), "An unexpected error occurred.",
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				request.getRequestURI(), false);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}