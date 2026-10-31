package com.sm.inventory_service.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockReleaseRequest(

		@NotNull(message = "Quantity is required") @Positive(message = "Quantity must be greater than zero") Integer quantity,

		@NotBlank(message = "Release reference is required") String releaseReference

) {
}