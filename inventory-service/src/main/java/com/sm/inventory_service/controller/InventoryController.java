package com.sm.inventory_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.inventory_service.request.dto.InventoryRequest;
import com.sm.inventory_service.response.dto.ApiResponse;
import com.sm.inventory_service.response.dto.InventoryResponseDto;
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

		InventoryResponseDto response =
                inventoryService.createInventory(request);
		
		ApiResponse<InventoryResponseDto> apiResponse =
	            ApiResponse.success(
	                    "Inventory created successfully",
	                    response
	            );

		return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body(apiResponse);
    }
	
	//product service calls this endpoint when de-activating product
	//while de-activating product also de-activate inventory
	//based on skucode
	//soft delete
	@PutMapping("/sku/{skuCode}/deactivate")
	public ResponseEntity<Void> deactivateInventory(
	        @PathVariable String skuCode) {

	    inventoryService.deactivateInventory(skuCode);

	    return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/sku/{skuCode}")
	public ResponseEntity<ApiResponse<InventoryResponseDto>> getInventoryBySkuCode(
	        @PathVariable String skuCode) {

	    ApiResponse<InventoryResponseDto> response =
	            inventoryService.getInventoryBySkuCode(skuCode);

	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/sku/{skuCode}/availability")
	public ResponseEntity<ApiResponse<StockAvailabilityResponse>>
	        checkStockAvailability(
	                @PathVariable String skuCode,
	                @RequestParam @Min(1) Integer quantity) {

	    StockAvailabilityResponse response =
	            inventoryService.checkStockAvailability(
	                    skuCode, quantity
	            );

	    return ResponseEntity.ok(
	            ApiResponse.success(
	                    "Stock availability checked successfully",
	                    response
	            )
	    );
	}


}
