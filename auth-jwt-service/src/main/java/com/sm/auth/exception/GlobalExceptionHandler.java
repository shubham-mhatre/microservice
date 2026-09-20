package com.sm.auth.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sm.auth.dto.ExceptionResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ExceptionResponseDto> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest req){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(),
				req.getRequestURI()
			);
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, HttpServletRequest req){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				req.getRequestURI()
			);
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
	}
}
