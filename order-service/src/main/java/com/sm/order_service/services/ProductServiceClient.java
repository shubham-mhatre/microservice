package com.sm.order_service.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.sm.order_service.dto.response.ApiResponse;
import com.sm.order_service.dto.response.ProductResponseDto;
import com.sm.order_service.exception.ResourceNotFoundException;
import com.sm.order_service.exception.ServiceDownException;

@Component
public class ProductServiceClient {
	
	@Value("${product.service.url}") 
	private String productServiceUrl;

    private final RestTemplate restTemplate;
    private final String PRODUCT_SERVICE_SKU_URL;
    public ProductServiceClient(
            @Value("${product.service.url}") String productServiceUrl,
            RestTemplate restTemplate) {

        this.productServiceUrl = productServiceUrl;
        this.restTemplate = restTemplate;
        this.PRODUCT_SERVICE_SKU_URL =
                productServiceUrl + "api/sku/{skuCode}";
    }

    public ProductResponseDto getProductBySkuCode(String skuCode) {
    	System.out.println("skucode "+skuCode);
        try {
            ResponseEntity<ApiResponse<ProductResponseDto>> response =
                    restTemplate.exchange(
                    		PRODUCT_SERVICE_SKU_URL,
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
