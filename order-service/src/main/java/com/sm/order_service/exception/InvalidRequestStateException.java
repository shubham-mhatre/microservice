package com.sm.order_service.exception;

public class InvalidRequestStateException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidRequestStateException(String msg) {
		super(msg);
	}

}
