package com.sm.order_service.services;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.sm.order_service.dto.response.ApiResponse;
import com.sm.order_service.dto.response.ProductResponseDto;
import com.sm.order_service.exception.ResourceNotFoundException;
import com.sm.order_service.exception.ServiceDownException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL =
            "http://product-service/api/products/sku/{skuCode}";

    public ProductResponseDto getProductBySkuCode(String skuCode) {

        try {
            ResponseEntity<ApiResponse<ProductResponseDto>> response =
                    restTemplate.exchange(
                            PRODUCT_SERVICE_URL,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<
                                    ApiResponse<ProductResponseDto>>() {},
                            skuCode
                    );

            ApiResponse<ProductResponseDto> body = response.getBody();

            if (body == null || body.getData() == null) {
                throw new ServiceDownException(
                        "Product Service returned an empty response.");
            }

            return body.getData();

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException(
                        "Product not found for SKU: " + skuCode);
            }

            throw new ServiceDownException(
                    "Product Service request failed with status: "
                            + ex.getStatusCode().value());

        } catch (ResourceAccessException | IllegalStateException ex) {
            throw new ServiceDownException(
                    "Product Service is currently unavailable.");
        }
    }
}
