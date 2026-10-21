package com.sm.inventory_service.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.inventory_service.exception.InvalidRequestStateException;
import com.sm.inventory_service.request.dto.InventoryRequest;
import com.sm.inventory_service.request.dto.StockAdjustmentRequest;
import com.sm.inventory_service.request.dto.StockQuantityUpdateRequest;
import com.sm.inventory_service.response.dto.ApiResponse;
import com.sm.inventory_service.response.dto.InventoryResponseDto;
import com.sm.inventory_service.response.dto.PageResponse;
import com.sm.inventory_service.response.dto.StockAvailabilityResponse;
import com.sm.inventory_service.service.InventoryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InventoryController {

	private final InventoryService inventoryService;

	@PostMapping
	public ResponseEntity<ApiResponse<InventoryResponseDto>> createInventory(
			@Valid @RequestBody InventoryRequest request) {

		InventoryResponseDto response = inventoryService.createInventory(request);

		ApiResponse<InventoryResponseDto> apiResponse = ApiResponse.success("Inventory created successfully", response);

		return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
	}

	// product service calls this endpoint when de-activating product
	// while de-activating product also de-activate inventory
	// based on skucode
	// soft delete
	@PutMapping("/sku/{skuCode}/deactivate")
	public ResponseEntity<Void> deactivateInventory(@PathVariable String skuCode) {

		inventoryService.deactivateInventory(skuCode);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/sku/{skuCode}")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryBySkuCode(@PathVariable String skuCode) {

		ApiResponse<InventoryResponseDto> response = inventoryService.getInventoryBySkuCode(skuCode);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/sku/{skuCode}/availability")
	public ResponseEntity<ApiResponse<StockAvailabilityResponse>> checkStockAvailability(@PathVariable String skuCode,
			@RequestParam @Min(1) Integer quantity) {

		StockAvailabilityResponse response = inventoryService.checkStockAvailability(skuCode, quantity);

		return ResponseEntity.ok(ApiResponse.success("Stock availability checked successfully", response));
	}

	@PatchMapping("/sku/{skuCode}/quantity")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> updateStockQuantity(@PathVariable String skuCode,
			@Valid @RequestBody StockQuantityUpdateRequest request) {

		InventoryResponseDto response = inventoryService.updateStockQuantity(skuCode, request.quantity());

		return ResponseEntity.ok(ApiResponse.success("Stock quantity updated successfully", response));
	}

	@PatchMapping("/sku/{skuCode}/adjust")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> adjustStock(@PathVariable String skuCode,
			@Valid @RequestBody StockAdjustmentRequest request) {

		InventoryResponseDto response = inventoryService.adjustStock(skuCode, request.adjustment());

		return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", response));
	}

	@PutMapping("/sku/{skuCode}/activate")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> activateInventory(@PathVariable String skuCode) {

		InventoryResponseDto response = inventoryService.activateInventory(skuCode);

		return ResponseEntity.ok(ApiResponse.success("Inventory activated successfully", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<InventoryResponseDto>>> getAllInventory(
			@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String direction) {

		// Restrict sorting to known entity properties.
		if (!Set.of("id", "skuCode", "quantity", "active").contains(sortBy)) {
			throw new InvalidRequestStateException("Invalid sortBy field. Allowed fields: id, skuCode, quantity, active");
		}

		if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
			throw new InvalidRequestStateException("Invalid direction. Allowed values: asc, desc");
		}

		PageResponse<InventoryResponseDto> response = inventoryService.getAllInventory(page, size, sortBy, direction);

		return ResponseEntity.ok(ApiResponse.success("Inventory fetched successfully", response));
	}

}
