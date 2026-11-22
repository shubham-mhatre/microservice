package com.sm.product_service.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class requests a service token from Auth Service 
 */
@Component
public class ServiceTokenClient {

	@Value("${auth.service.url}")
	private String authServiceUrl;

	@Value("${security.service.client-id}")
	private String clientId;

	@Value("${security.service.client-secret}")
	private String clientSecret;

	private final RestTemplate restTemplate;

	public ServiceTokenClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getServiceToken() {

		String url = authServiceUrl + "service-token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, String> request = Map.of("clientId", clientId, "clientSecret", clientSecret);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

		JsonNode response = restTemplate.postForObject(url, entity, JsonNode.class);

		JsonNode data = response.path("data");

		String token = data.path("token").asText(null);

		if (token == null || token.isBlank()) {
		    token = data.path("accessToken").asText(null);
		}

		if (token == null || token.isBlank()) {
		    throw new IllegalStateException(
		            "Auth Service response does not contain a valid token");
		}

		return token;
	}
}