package com.sm.api_gateway.dto;

import java.time.LocalDateTime;


public class ExceptionResponseDto {

	private LocalDateTime time;
	private String message;
	private Integer status;
	private String error;
	private String path;	
	
	public ExceptionResponseDto(LocalDateTime time, String message, Integer status, String error, String path) {
		this.time = time;
		this.message = message;
		this.status = status;
		this.error = error;
		this.path = path;
	}
	
	
	public LocalDateTime getTime() {
		return time;
	}
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	
}
