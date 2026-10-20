package com.sm.api_gateway.exception;

public class MissingAuthHeaderException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MissingAuthHeaderException(String msg) {
		super(msg);
	}
	
}
