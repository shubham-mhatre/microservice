
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

	private final RestTemplate restTemplate;

	/**
	 * Reserves the requested quantity from inventory.
	 *
	 * @param skuCode  product SKU code
	 * @param quantity quantity to reserve
	 */
	public void reserveStock(String skuCode, int quantity) {

		String url = inventoryServiceUrl + "api/sku/{skuCode}/reserve";

		Map<String, Integer> requestBody = Map.of("quantity", quantity);

		executeStockOperation(url, skuCode, requestBody);
	}

	/**
	 * Releases previously reserved stock.
	 *
	 * @param skuCode          product SKU code
	 * @param quantity         quantity to release
	 * @param releaseReference unique reference to prevent duplicate stock releases
	 */
	public void releaseStock(String skuCode, int quantity, String releaseReference) {

		String url = inventoryServiceUrl + "api/sku/{skuCode}/release";

		Map<String, Object> requestBody = Map.of("quantity", quantity, "releaseReference", releaseReference);

		executeStockOperation(url, skuCode, requestBody);
	}

	/**
	 * Executes a stock operation against Inventory Service.
	 *
	 * @param url         inventory API URL
	 * @param skuCode     product SKU code
	 * @param requestBody request payload
	 */
	private void executeStockOperation(String url, String skuCode, Map<String, ?> requestBody) {

		HttpEntity<Map<String, ?>> request = new HttpEntity<>(requestBody);

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
