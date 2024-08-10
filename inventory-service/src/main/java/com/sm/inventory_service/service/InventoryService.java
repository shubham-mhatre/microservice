package com.sm.inventory_service.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.entity.repository.InventoryRepository;
import com.sm.inventory_service.response.dto.InventoryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryRepository inventoryRepository;

	public List<InventoryResponseDto> isInStock(List<String> skuCodes) {

		List<Inventory> inventoryList= inventoryRepository.findBySkuCodeIn(skuCodes);
		
		Map<String, Inventory> inventoryMap = inventoryList.stream()
	            .collect(Collectors.toMap(Inventory::getSkuCode, inventory -> inventory));

	    // Iterate over the input skuCodes list and create response based on inventory data
	    return skuCodes.stream()
	            .map(skuCode -> {
	                Inventory inventory = inventoryMap.get(skuCode);
	                boolean isPresent = inventory != null && inventory.getQuantity() > 0;
	                return new InventoryResponseDto(skuCode, isPresent);
	            })
	            .toList();
		
	}
}
