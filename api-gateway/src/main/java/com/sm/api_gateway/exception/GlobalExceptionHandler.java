package com.sm.api_gateway.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import com.sm.api_gateway.dto.ExceptionResponseDto;


@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(MissingAuthHeaderException.class)
	public ResponseEntity<ExceptionResponseDto> handleMissingAuthHeaderException(Exception ex, ServerWebExchange exchange){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				exchange.getRequest().getURI().toString(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
	}
	
	@ExceptionHandler(UnAuthorizeAccessException.class)
	public ResponseEntity<ExceptionResponseDto> handleUnAuthorizeException(Exception ex, ServerWebExchange exchange){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				exchange.getRequest().getURI().toString(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, ServerWebExchange exchange){
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
				LocalDateTime.now(),
				ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				exchange.getRequest().getURI().toString(),
				Boolean.FALSE
			);
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
	}
}
