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
import com.sm.inventory_service.request.dto.StockReleaseRequest;
import com.sm.inventory_service.request.dto.StockReservationRequest;
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

	/**
	 * Creates a new inventory record for the given SKU.
	 *
	 * @param request inventory details, including SKU code and initial quantity
	 * @return HTTP 201 Created with the created inventory details
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<InventoryResponseDto>> createInventory(
			@Valid @RequestBody InventoryRequest request) {

		InventoryResponseDto response = inventoryService.createInventory(request);

		ApiResponse<InventoryResponseDto> apiResponse = ApiResponse.success("Inventory created successfully", response);

		return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
	}

	/**
	 * Deactivates inventory for the specified SKU without deleting the record.
	 * Used by Product Service when a product is deactivated.
	 *
	 * @param skuCode SKU code of the inventory to deactivate
	 * @return HTTP 204 No Content on successful deactivation
	 */
	@PutMapping("/sku/{skuCode}/deactivate")
	public ResponseEntity<Void> deactivateInventory(@PathVariable String skuCode) {

		inventoryService.deactivateInventory(skuCode);

		return ResponseEntity.noContent().build();
	}

	/**
	 * Retrieves inventory details for the specified SKU.
	 *
	 * @param skuCode SKU code to look up
	 * @return HTTP 200 OK with the inventory details
	 */
	@GetMapping("/sku/{skuCode}")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryBySkuCode(@PathVariable String skuCode) {

		ApiResponse<InventoryResponseDto> response = inventoryService.getInventoryBySkuCode(skuCode);

		return ResponseEntity.ok(response);
	}

	/**
	 * Checks whether the requested quantity is available for the specified SKU.
	 *
	 * @param skuCode SKU code to check
	 * @param quantity requested quantity; must be at least 1
	 * @return HTTP 200 OK with stock availability details
	 */
	@GetMapping("/sku/{skuCode}/availability")
	public ResponseEntity<ApiResponse<StockAvailabilityResponse>> checkStockAvailability(@PathVariable String skuCode,
			@RequestParam @Min(1) Integer quantity) {

		StockAvailabilityResponse response = inventoryService.checkStockAvailability(skuCode, quantity);

		return ResponseEntity.ok(ApiResponse.success("Stock availability checked successfully", response));
	}

	/**
	 * Sets the stock quantity for the specified SKU to an exact value.
	 *
	 * @param skuCode SKU code whose stock quantity will be updated
	 * @param request new stock quantity; must not be negative
	 * @return HTTP 200 OK with the updated inventory details
	 */
	@PatchMapping("/sku/{skuCode}/quantity")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> updateStockQuantity(@PathVariable String skuCode,
			@Valid @RequestBody StockQuantityUpdateRequest request) {

		InventoryResponseDto response = inventoryService.updateStockQuantity(skuCode, request.quantity());

		return ResponseEntity.ok(ApiResponse.success("Stock quantity updated successfully", response));
	}

	/**
	 * Increases or decreases stock by the specified adjustment amount.
	 *
	 * @param skuCode SKU code whose stock will be adjusted
	 * @param request stock adjustment; positive to increase and negative to decrease
	 * @return HTTP 200 OK with the updated inventory details
	 */
	@PatchMapping("/sku/{skuCode}/adjust")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> adjustStock(@PathVariable String skuCode,
			@Valid @RequestBody StockAdjustmentRequest request) {

		InventoryResponseDto response = inventoryService.adjustStock(skuCode, request.adjustment());

		return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", response));
	}

	/**
	 * Activates inventory for the specified SKU without changing its quantity.
	 *
	 * @param skuCode SKU code of the inventory to activate
	 * @return HTTP 200 OK with the updated inventory details
	 */
	@PutMapping("/sku/{skuCode}/activate")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> activateInventory(@PathVariable String skuCode) {

		InventoryResponseDto response = inventoryService.activateInventory(skuCode);

		return ResponseEntity.ok(ApiResponse.success("Inventory activated successfully", response));
	}

	/**
	 * Retrieves a paginated list of inventory records with configurable sorting.
	 *
	 * @param page zero-based page number
	 * @param size number of records per page
	 * @param sortBy inventory field used for sorting
	 * @param direction sort direction: asc or desc
	 * @return HTTP 200 OK with paginated inventory details
	 */
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
	
	/**
	 * Reserves stock for order processing.
	 */
	@PostMapping("/sku/{skuCode}/reserve")
	public ResponseEntity<Void> reserveStock(
	        @PathVariable String skuCode,
	        @Valid @RequestBody StockReservationRequest request) {

	    inventoryService.reserveStock(skuCode, request.quantity());

	    return ResponseEntity.noContent().build();
	}

	/**
	 * Releases previously reserved stock.
	 */
	@PostMapping("/sku/{skuCode}/release")
	public ResponseEntity<ApiResponse<Void>> releaseStock(
	        @PathVariable String skuCode,
	        @Valid @RequestBody StockReleaseRequest request) {

	    inventoryService.releaseStock(skuCode, request);

	    return ResponseEntity.ok(
	            ApiResponse.success("Inventory stock released successfully", null));
	}

}
