package com.sm.inventory_service.exception;

public class InvalidStockAdjustmentException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidStockAdjustmentException(String message) {
        super(message);
    }
}
