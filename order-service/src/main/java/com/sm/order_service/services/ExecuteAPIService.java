package com.sm.order_service.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.sm.order_service.dto.response.InventoryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecuteAPIService {

	@Value("${inventory.service.uri}")
	private String inventoryServiceUrl;

	private final RestTemplate restTemplate;

	public List<InventoryResponseDto> executeInventoryBySku1(List<String> skuList) {
        String url = UriComponentsBuilder.fromUriString(inventoryServiceUrl)
                .queryParam("skuCode", String.join(",", skuList))
                .toUriString();

        InventoryResponseDto[] response = restTemplate.getForObject(url, InventoryResponseDto[].class);

        return Arrays.asList(response);
    }

}
