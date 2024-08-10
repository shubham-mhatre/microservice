package com.sm.inventory_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.entity.repository.InventoryRepository;
import com.sm.inventory_service.response.dto.InventoryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryRepository inventoryRepository;

	public List<InventoryResponseDto> isInStock(List<String> skuCode) {

		List<Inventory> inventory= inventoryRepository.findBySkuCodeIn(skuCode);

		return  inventory.stream()
				.map(data->new InventoryResponseDto(data.getSkuCode(),data.getQuantity()>0))
				.toList();
		
	}
}
