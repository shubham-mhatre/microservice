package com.sm.inventory_service.service;

import org.springframework.stereotype.Service;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.entity.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryRepository inventoryRepository;

	public boolean isInStock(String skuCode) {
		boolean isPresent=true;
		Inventory inventory= inventoryRepository.findBySkuCode(skuCode);

		if(inventory==null) {
			isPresent= false;
		}
		return isPresent;
	}
}
