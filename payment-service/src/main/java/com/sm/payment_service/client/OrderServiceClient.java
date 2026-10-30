
package com.sm.payment_service.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sm.payment_service.dto.response.OrderDetailsResponse;
import com.sm.payment_service.dto.response.OrderServiceApiResponse;
import com.sm.payment_service.exception.InvalidRequestStateException;
import com.sm.payment_service.exception.OrderNotFoundException;
import com.sm.payment_service.exception.OrderServiceUnavailableException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderServiceClient {

	@Value("${order.service.url}")
	private String orderServiceUrl;

	private final RestTemplate restTemplate;

	public OrderDetailsResponse getOrderById(Long orderId) {

		String url = orderServiceUrl + "api/" + orderId;

		try {
			ResponseEntity<OrderServiceApiResponse> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<OrderServiceApiResponse>() {
					});

			OrderServiceApiResponse body = response.getBody();

			if (body == null || !body.isSuccess() || body.getData() == null) {
				throw new OrderServiceUnavailableException("Order Service returned an invalid response.");
			}

			return body.getData();

		} catch (HttpClientErrorException.NotFound ex) {

			// Order Service responded with HTTP 404.
			throw new OrderNotFoundException("Order not found with ID: " + orderId);

		} catch (RestClientException ex) {

			// HTTP errors other than 404, connection failures,
			// and other REST communication failures.
			throw new OrderServiceUnavailableException("Unable to communicate with Order Service.", ex);

		} catch (IllegalStateException ex) {

			// For example: No instances available for order-service.
			throw new OrderServiceUnavailableException("Order Service is currently unavailable.", ex);
		}
	}

	public void updateOrderStatus(Long orderId, String status) {

		String url = orderServiceUrl + "api/" + orderId + "/status";

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, String> requestBody = Map.of("status", status);

			HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<OrderServiceApiResponse> response = restTemplate.exchange(url, HttpMethod.PATCH,
					requestEntity, new ParameterizedTypeReference<OrderServiceApiResponse>() {
					});

			OrderServiceApiResponse body = response.getBody();

			if (body == null || !body.isSuccess()) {
				throw new OrderServiceUnavailableException("Order Service returned an invalid response.");
			}

		} catch (HttpClientErrorException.NotFound ex) {

			throw new OrderNotFoundException("Order not found with ID: " + orderId);

		} catch (HttpClientErrorException.Conflict ex) {

			throw new InvalidRequestStateException(
					"Order Service rejected the status update: " + ex.getResponseBodyAsString());

		} catch (HttpClientErrorException.BadRequest ex) {

			throw new InvalidRequestStateException("Invalid order status update: " + ex.getResponseBodyAsString());

		} catch (RestClientException ex) {

			throw new OrderServiceUnavailableException("Unable to communicate with Order Service.", ex);

		} catch (IllegalStateException ex) {

			throw new OrderServiceUnavailableException("Order Service is currently unavailable.", ex);
		}
	}
}
