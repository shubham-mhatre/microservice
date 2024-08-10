package com.sm.order_service.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.sm.order_service.dto.response.InventoryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecuteAPIService {

	@Value("${inventory.service.uri}")
	private String inventoryServiceUrl;

	private final WebClient webClient;


	public List<InventoryResponseDto> executeInventoryBySku(List<String> skuList){

		InventoryResponseDto[] response=webClient.get()
				.uri(inventoryServiceUrl,urlBuilder -> 
				urlBuilder.queryParam("skuCode", skuList).build())
				.retrieve()
				.bodyToMono(InventoryResponseDto[].class)
				.block();		

		return Arrays.asList(response);
	}

}
