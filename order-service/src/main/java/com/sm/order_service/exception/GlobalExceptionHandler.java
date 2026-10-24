package com.sm.order_service.exception;

import java.time.LocalDateTime;

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
	public ResponseEntity<ExceptionResponseDto> handleResourceNotFoundException(Exception ex, HttpServletRequest request) {
		
		log.error("ResourceNotFoundException on {}", request.getRequestURI(), ex);
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(LocalDateTime.now(), ex.getMessage(),
				HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
				request.getRequestURI(), Boolean.FALSE);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
	}
	
	
}
