package com.sm.product_service.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;


import com.sm.product_service.dto.request.InventoryRequest;
import com.sm.product_service.dto.response.InventoryResponseDto;

@Component
public class InventoryServiceClient {
	
	@Value("${inventory.service.url}") 
	private String inventoryServiceUrl;

	private final RestTemplate restTemplate;

	public InventoryServiceClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void createInventory(String skuCode) {

		InventoryRequest request = new InventoryRequest(skuCode, 0);

		String url = inventoryServiceUrl + "inventory/api";

		restTemplate.postForEntity(url, request, InventoryResponseDto.class);
	}
}
