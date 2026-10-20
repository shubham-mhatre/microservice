package com.sm.inventory_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.entity.repository.InventoryRepository;
import com.sm.inventory_service.exception.DuplicateResourceException;
import com.sm.inventory_service.mapper.InventoryMapper;
import com.sm.inventory_service.request.dto.InventoryRequest;
import com.sm.inventory_service.response.dto.InventoryResponseDto;


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

		return new InventoryResponseDto(savedInventory.getId(), savedInventory.getSkuCode(), savedInventory.getQuantity());
	}

}
