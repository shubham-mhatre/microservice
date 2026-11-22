package com.sm.product_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.sm.product_service.dto.request.InventoryRequest;
import com.sm.product_service.dto.response.InventoryResponseDto;
import com.sm.product_service.exception.ServiceDownException;

@Component
public class InventoryServiceClient {

	@Value("${inventory.service.url}")
	private String inventoryServiceUrl;

	private final RestTemplate restTemplate;
	private final ServiceTokenProvider serviceTokenProvider;

	public InventoryServiceClient(RestTemplate restTemplate, ServiceTokenProvider serviceTokenProvider) {

		this.restTemplate = restTemplate;
		this.serviceTokenProvider = serviceTokenProvider;
	}

	public void createInventory(String skuCode) {

		InventoryRequest request = new InventoryRequest(skuCode, 0);

		String url = inventoryServiceUrl + "api";

		try {
			HttpEntity<InventoryRequest> entity = new HttpEntity<>(request, getHeaders());

			restTemplate.exchange(url, HttpMethod.POST, entity, InventoryResponseDto.class);

		} catch (ResourceAccessException ex) {
			throw new ServiceDownException(
					"Inventory Service is currently unavailable. " + "Product creation could not be completed.");
		}
	}

	public void deactivateInventory(String skuCode) {

		String url = inventoryServiceUrl + "api/sku/" + skuCode + "/deactivate";

		try {
			HttpEntity<Void> entity = new HttpEntity<>(getHeaders());

			restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

		} catch (ResourceAccessException ex) {
			throw new ServiceDownException(
					"Inventory Service is currently unavailable. " + "Product deactivation could not be completed.");
		}
	}

	private HttpHeaders getHeaders() {

		HttpHeaders headers = new HttpHeaders();

		headers.setBearerAuth(serviceTokenProvider.getToken());
		headers.setContentType(MediaType.APPLICATION_JSON);

		return headers;
	}
}