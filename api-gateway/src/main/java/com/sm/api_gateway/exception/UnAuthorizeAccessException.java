package com.sm.api_gateway.exception;

public class UnAuthorizeAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnAuthorizeAccessException(String msg) {
		super(msg);
	}
}
