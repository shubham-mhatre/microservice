
package com.sm.auth.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sm.auth.dto.ExceptionResponseDto;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ExceptionResponseDto> handleDuplicateResourceException(DuplicateResourceException ex,
			HttpServletRequest req) {

		return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, req);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(AuthenticationException ex,
			HttpServletRequest req) {

		return buildErrorResponse("Invalid username or password", HttpStatus.UNAUTHORIZED, req);
	}

	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ExceptionResponseDto> handleJwtException(JwtException ex, HttpServletRequest req) {

		return buildErrorResponse("Invalid or expired JWT token", HttpStatus.UNAUTHORIZED, req);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponseDto> handleValidationException(MethodArgumentNotValidException ex,
			HttpServletRequest req) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return buildErrorResponse(errors.toString(), HttpStatus.BAD_REQUEST, req);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, HttpServletRequest req) {

		return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, req);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ExceptionResponseDto> handleIllegalArgumentException(IllegalArgumentException ex,
			HttpServletRequest req) {

		return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, req);
	}

	private ResponseEntity<ExceptionResponseDto> buildErrorResponse(String message, HttpStatus status,
			HttpServletRequest req) {

		ExceptionResponseDto response = new ExceptionResponseDto(LocalDateTime.now(), message, status.value(),
				status.getReasonPhrase(), req.getRequestURI(), false);

		return ResponseEntity.status(status).body(response);
	}
}
