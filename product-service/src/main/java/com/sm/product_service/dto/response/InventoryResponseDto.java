package com.sm.product_service.dto.response;

public record InventoryResponseDto(
        Integer id,
        String skuCode,
        Integer quantity
) {}