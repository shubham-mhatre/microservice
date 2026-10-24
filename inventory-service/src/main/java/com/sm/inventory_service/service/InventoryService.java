package com.sm.inventory_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.entity.repository.InventoryRepository;
import com.sm.inventory_service.exception.DuplicateResourceException;
import com.sm.inventory_service.exception.InactiveInventoryException;
import com.sm.inventory_service.exception.InsufficientStockException;
import com.sm.inventory_service.exception.InvalidRequestStateException;
import com.sm.inventory_service.exception.InvalidStockAdjustmentException;
import com.sm.inventory_service.exception.ResourceNotFoundException;
import com.sm.inventory_service.mapper.InventoryMapper;
import com.sm.inventory_service.request.dto.InventoryRequest;
import com.sm.inventory_service.response.dto.ApiResponse;
import com.sm.inventory_service.response.dto.InventoryResponseDto;
import com.sm.inventory_service.response.dto.PageResponse;
import com.sm.inventory_service.response.dto.StockAvailabilityResponse;

@Service
public class InventoryService {

	private final InventoryRepository inventoryRepository;

	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	@Transactional
	public InventoryResponseDto createInventory(InventoryRequest request) {

		String skuCode = request.getSkuCode().trim();

		if (inventoryRepository.existsBySkuCode(skuCode)) {
			throw new DuplicateResourceException("Inventory already exists for SKU: " + skuCode);
		}

		Inventory inventory = InventoryMapper.INSTANCE.toEntity(skuCode, request);

		Inventory savedInventory = inventoryRepository.save(inventory);

		return new InventoryResponseDto(savedInventory.getId(), savedInventory.getSkuCode(),
				savedInventory.getQuantity(), false);
	}

	@Transactional
	public void deactivateInventory(String skuCode) {

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		inventory.setActive(false);

		inventoryRepository.save(inventory);
	}

	public ApiResponse<InventoryResponseDto> getInventoryBySkuCode(String skuCode) {

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		InventoryResponseDto response = new InventoryResponseDto(inventory.getId(), inventory.getSkuCode(),
				inventory.getQuantity(), inventory.isActive());

		return ApiResponse.success("Inventory fetched successfully", response);
	}

	@Transactional(readOnly = true)
	public StockAvailabilityResponse checkStockAvailability(String skuCode, Integer requestedQuantity) {

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		boolean available = inventory.isActive() && inventory.getQuantity() >= requestedQuantity;

		return new StockAvailabilityResponse(inventory.getSkuCode(), requestedQuantity, inventory.getQuantity(),
				inventory.isActive(), available);
	}

	@Transactional
	public InventoryResponseDto updateStockQuantity(String skuCode, Integer quantity) {

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		if (!inventory.isActive()) {
			throw new InactiveInventoryException("Cannot update stock for inactive inventory: " + skuCode);
		}

		inventory.setQuantity(quantity);

		Inventory updatedInventory = inventoryRepository.save(inventory);

		return new InventoryResponseDto(updatedInventory.getId(), updatedInventory.getSkuCode(),
				updatedInventory.getQuantity(), updatedInventory.isActive());
	}

	@Transactional
	public InventoryResponseDto adjustStock(String skuCode, Integer adjustment) {

		if (adjustment == null || adjustment == 0) {
			throw new InvalidStockAdjustmentException("Stock adjustment must not be zero");
		}

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		if (!inventory.isActive()) {
			throw new InactiveInventoryException("Cannot adjust stock for inactive inventory: " + skuCode);
		}

		long updatedQuantity = (long) inventory.getQuantity() + adjustment;

		if (updatedQuantity < 0) {
			throw new InsufficientStockException("Insufficient stock for SKU: " + skuCode + ". Available quantity: "
					+ inventory.getQuantity() + ", requested decrease: " + -(long) adjustment);
		}

		if (updatedQuantity > Integer.MAX_VALUE) {
			throw new InvalidStockAdjustmentException("Adjusted quantity exceeds the maximum supported value");
		}

		inventory.setQuantity((int) updatedQuantity);

		Inventory updatedInventory = inventoryRepository.save(inventory);

		return new InventoryResponseDto(updatedInventory.getId(), updatedInventory.getSkuCode(),
				updatedInventory.getQuantity(), updatedInventory.isActive());
	}

	@Transactional
	public InventoryResponseDto activateInventory(String skuCode) {

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		if (!inventory.isActive()) {
			inventory.setActive(true);
			inventoryRepository.save(inventory);
		}

		return new InventoryResponseDto(inventory.getId(), inventory.getSkuCode(), inventory.getQuantity(),
				inventory.isActive());
	}

	@Transactional(readOnly = true)
	public PageResponse<InventoryResponseDto> getAllInventory(int page, int size, String sortBy, String direction) {

		Sort.Direction sortDirection = Sort.Direction.fromString(direction);

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

		Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);

		List<InventoryResponseDto> inventoryResponses = inventoryPage.getContent().stream()
				.map(inventory -> new InventoryResponseDto(inventory.getId(), inventory.getSkuCode(),
						inventory.getQuantity(), inventory.isActive()))
				.toList();

		return new PageResponse<>(inventoryResponses, inventoryPage.getNumber(), inventoryPage.getSize(),
				inventoryPage.getTotalElements(), inventoryPage.getTotalPages(), inventoryPage.isFirst(),
				inventoryPage.isLast());
	}

	@Transactional
	public void reserveStock(String skuCode, Integer quantity) {

		int updatedRows = inventoryRepository.reserveStock(skuCode, quantity);

		if (updatedRows == 1) {
			return;
		}

		Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + skuCode));

		if (!inventory.isActive()) {
			throw new InvalidRequestStateException("Inventory is inactive for SKU: " + skuCode);
		}

		throw new InvalidRequestStateException("Insufficient stock for SKU: " + skuCode);
	}

	@Transactional
	public void releaseStock(String skuCode, Integer quantity) {

		int updatedRows = inventoryRepository.releaseStock(skuCode, quantity);

		if (updatedRows == 0) {
			throw new ResourceNotFoundException("Inventory not found or quantity is unavailable for SKU: " + skuCode);
		}
	}

}
