package com.sm.product_service.exception;

public class ServiceDownException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ServiceDownException(String msg) {
		super(msg);
	}

}
