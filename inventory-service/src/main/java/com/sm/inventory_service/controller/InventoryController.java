package com.sm.inventory_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sm.inventory_service.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InventoryController {
	
	private final InventoryService inventoryService;

	@GetMapping("/{sku-code}")
	public boolean isInStock(@PathVariable("sku-code") String skuCode) {
		return inventoryService.isInStock(skuCode);
	}
}
