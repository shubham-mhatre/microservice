package com.sm.inventory_service.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sm.inventory_service.response.dto.ExceptionResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ExceptionResponseDto> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest req){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(),
				req.getRequestURI(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
	}
	
	@ExceptionHandler(InactiveInventoryException.class)
	public ResponseEntity<ExceptionResponseDto> handleDuplicateResourceException(InactiveInventoryException ex, HttpServletRequest req){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(),
				req.getRequestURI(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
	}
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ExceptionResponseDto> resourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest req){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				req.getRequestURI(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponseDto> handleConstraintViolationException(
	        ConstraintViolationException ex,
	        HttpServletRequest req) {

		String message = ex.getConstraintViolations()
	            .stream()
	            .findFirst()
	            .map(violation ->
	                    violation.getPropertyPath() + ": "
	                    + violation.getMessage()
	            )
	            .orElse("Invalid request parameter");

	    ExceptionResponseDto response = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            message,
	            HttpStatus.BAD_REQUEST.value(),
	            HttpStatus.BAD_REQUEST.getReasonPhrase(),
	            req.getRequestURI(),
	            Boolean.FALSE
	    );

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(response);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

		String message = ex.getBindingResult()
	            .getFieldErrors()
	            .stream()
	            .findFirst()
	            .map(error -> error.getDefaultMessage())
	            .orElse("Invalid request");

	    ExceptionResponseDto response = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            message,
	            HttpStatus.BAD_REQUEST.value(),
	            HttpStatus.BAD_REQUEST.getReasonPhrase(),
	            req.getRequestURI(),
	            Boolean.FALSE
	    );

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(response);
	}
	
	@ExceptionHandler(InvalidStockAdjustmentException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidStockAdjustment(
	        InvalidStockAdjustmentException ex,
	        HttpServletRequest req) {

	    ExceptionResponseDto response = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            ex.getMessage(),
	            HttpStatus.BAD_REQUEST.value(),
	            HttpStatus.BAD_REQUEST.getReasonPhrase(),
	            req.getRequestURI(),
	            Boolean.FALSE
	    );

	    return ResponseEntity.badRequest().body(response);
	}
	
	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ExceptionResponseDto> handleInsufficientStock(
	        InsufficientStockException ex,
	        HttpServletRequest req) {

	    ExceptionResponseDto response = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            ex.getMessage(),
	            HttpStatus.CONFLICT.value(),
	            HttpStatus.CONFLICT.getReasonPhrase(),
	            req.getRequestURI(),
	            Boolean.FALSE
	    );

	    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}
	
	@ExceptionHandler(InvalidRequestStateException.class)
	public ResponseEntity<ExceptionResponseDto> handleInvalidRequestStateException(
			InvalidRequestStateException ex,
	        HttpServletRequest req) {

	    ExceptionResponseDto response = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            ex.getMessage(),
	            HttpStatus.CONFLICT.value(),
	            HttpStatus.CONFLICT.getReasonPhrase(),
	            req.getRequestURI(),
	            Boolean.FALSE
	    );

	    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, HttpServletRequest req){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				req.getRequestURI(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
	}
}
