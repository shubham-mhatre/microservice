package com.sm.inventory_service.response.dto;

public record StockAvailabilityResponse(
        String skuCode,
        Integer requestedQuantity,
        Integer availableQuantity,
        boolean active,
        boolean available
) {
}
