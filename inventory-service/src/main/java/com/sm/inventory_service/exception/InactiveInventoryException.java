package com.sm.inventory_service.exception;

public class InactiveInventoryException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InactiveInventoryException(String msg) {
		super(msg);
	}
}
