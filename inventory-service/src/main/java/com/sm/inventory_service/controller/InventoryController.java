package com.sm.inventory_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.inventory_service.response.dto.InventoryResponseDto;
import com.sm.inventory_service.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InventoryController {
	
	private final InventoryService inventoryService;

	@GetMapping
	public ResponseEntity<List<InventoryResponseDto>> isInStock(@RequestParam("sku-code") List<String> skuCode) {
		return ResponseEntity.ok(inventoryService.isInStock(skuCode));
	}
}
