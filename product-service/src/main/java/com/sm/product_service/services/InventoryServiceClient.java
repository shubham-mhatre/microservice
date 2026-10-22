package com.sm.product_service.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;


import com.sm.product_service.dto.request.InventoryRequest;
import com.sm.product_service.dto.response.InventoryResponseDto;
import com.sm.product_service.exception.ServiceDownException;

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

		try {
            restTemplate.postForEntity(
                    url,
                    request,
                    InventoryResponseDto.class
            );
        } catch (ResourceAccessException | IllegalStateException exex) {
            throw new ServiceDownException(
                    "Inventory Service is currently unavailable. "
                            + "Product creation could not be completed."
            );
        }
	}
	
	public void deactivateInventory(String skuCode) {

        String url = inventoryServiceUrl
                + "inventory/api/sku/"
                + skuCode
                + "/deactivate";

        restTemplate.put(url, null);
    }
}
