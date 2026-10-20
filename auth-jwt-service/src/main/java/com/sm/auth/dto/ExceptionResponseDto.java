package com.sm.auth.dto;

import java.time.LocalDateTime;


public class ExceptionResponseDto {

	private LocalDateTime time;
	private String message;
	private Integer status;
	private String error;
	private String path;	
	private Boolean success;
	
	public ExceptionResponseDto(LocalDateTime time, String message, Integer status, String error, String path,Boolean success) {
		this.time = time;
		this.message = message;
		this.status = status;
		this.error = error;
		this.path = path;
		this.success = success;
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


	public Boolean getSuccess() {
		return success;
	}


	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
	
}
