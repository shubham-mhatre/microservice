package com.sm.inventory_service.exception;

public class DuplicateResourceException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateResourceException(String msg) {
		super(msg);
	}
	
}
