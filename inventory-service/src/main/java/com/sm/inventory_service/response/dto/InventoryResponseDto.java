package com.sm.inventory_service.response.dto;

public record InventoryResponseDto(Integer id, String skuCode, 
		Integer quantity, boolean active) {
}