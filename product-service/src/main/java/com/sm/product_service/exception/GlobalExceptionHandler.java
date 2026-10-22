package com.sm.product_service.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import com.sm.product_service.dto.response.ExceptionResponseDto;

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
	
	@ExceptionHandler(HttpStatusCodeException.class)
	public ResponseEntity<ExceptionResponseDto> handleHttpStatusCodeException(
	        HttpStatusCodeException ex,
	        HttpServletRequest request) {

	    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

	    log.error("HttpStatusCodeException : Downstream service error on {}: {}",
	            request.getRequestURI(), ex.getMessage());

	    ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            extractErrorMessage(ex),
	            status.value(),
	            status.getReasonPhrase(),
	            request.getRequestURI(),
	            Boolean.FALSE
	    );

	    return ResponseEntity.status(ex.getStatusCode())
	            .body(exceptionResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
		
		log.error("Unhandled exception on {}", request.getRequestURI(), ex);
		
		ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(LocalDateTime.now(), ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				request.getRequestURI(), Boolean.FALSE);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
	}
	
	@ExceptionHandler(ServiceDownException.class)
	public ResponseEntity<ExceptionResponseDto> handleServiceDownException(
			ServiceDownException ex,
	        HttpServletRequest request) {

	    ExceptionResponseDto response = new ExceptionResponseDto(
	            LocalDateTime.now(),
	            ex.getMessage(),
	            HttpStatus.SERVICE_UNAVAILABLE.value(),
	            HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
	            request.getRequestURI(),
	            false
	    );

	    return ResponseEntity
	            .status(HttpStatus.SERVICE_UNAVAILABLE)
	            .body(response);
	}
	
	private String extractErrorMessage(HttpStatusCodeException ex) {
	    try {
	        com.fasterxml.jackson.databind.JsonNode jsonNode =
	                new com.fasterxml.jackson.databind.ObjectMapper()
	                        .readTree(ex.getResponseBodyAsString());

	        return jsonNode.has("message")
	                ? jsonNode.get("message").asText()
	                : ex.getStatusText();

	    } catch (Exception exception) {
	        return ex.getStatusText();
	    }
	}
}
