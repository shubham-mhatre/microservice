package com.sm.order_service.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.sm.order_service.exception.InvalidRequestStateException;
import com.sm.order_service.exception.ResourceNotFoundException;
import com.sm.order_service.exception.ServiceDownException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryServiceClient {
	
	@Value("${inventory.service.url}") 
	private String inventoryServiceUrl;

	private final String RESERVE_URL = inventoryServiceUrl+"api/sku/{skuCode}/reserve";

	private final String RELEASE_URL = inventoryServiceUrl+"api/sku/{skuCode}/release";

	private final RestTemplate restTemplate;

	public void reserveStock(String skuCode, int quantity) {
		executeStockOperation(RESERVE_URL, skuCode, quantity);
	}

	public void releaseStock(String skuCode, int quantity) {
		executeStockOperation(RELEASE_URL, skuCode, quantity);
	}

	private void executeStockOperation(String url, String skuCode, int quantity) {

		Map<String, Integer> requestBody = Map.of("quantity", quantity);

		HttpEntity<Map<String, Integer>> request = new HttpEntity<>(requestBody);

		try {
			ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, request, Void.class, skuCode);

			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new ServiceDownException("Inventory Service operation failed.");
			}

		} catch (RestClientResponseException ex) {

			HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

			if (status == HttpStatus.NOT_FOUND) {
				throw new ResourceNotFoundException("Inventory not found for SKU: " + skuCode);
			}

			if (status == HttpStatus.CONFLICT || status == HttpStatus.BAD_REQUEST) {
				throw new InvalidRequestStateException("Inventory operation rejected for SKU: " + skuCode);
			}

			throw new ServiceDownException(
					"Inventory Service request failed with status: " + ex.getStatusCode().value());

		} catch (ResourceAccessException | IllegalStateException ex) {
			throw new ServiceDownException("Inventory Service is currently unavailable.");
		}
	}
}
