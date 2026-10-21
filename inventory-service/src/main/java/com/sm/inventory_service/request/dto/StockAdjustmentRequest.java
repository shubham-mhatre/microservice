package com.sm.inventory_service.request.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(

        @NotNull(message = "Adjustment is required")
        Integer adjustment

) {
}
