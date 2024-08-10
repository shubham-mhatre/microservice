package com.sm.product_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sm.product_service.dto.request.ProductRequest;
import com.sm.product_service.dto.response.ProductResponse;
import com.sm.product_service.services.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class Productcontroller {
	
	private final ProductService productService;

	@PostMapping("/save")
	public ResponseEntity<ProductResponse> saveProduct(@RequestBody ProductRequest request) {
		ProductResponse productResponse= productService.saveProduct(request);
		return  ResponseEntity.ok(productResponse);
	}
}
