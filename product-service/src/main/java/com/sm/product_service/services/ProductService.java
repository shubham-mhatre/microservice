package com.sm.product_service.services;

import org.springframework.stereotype.Service;

import com.sm.product_service.dto.request.ProductRequest;
import com.sm.product_service.dto.response.ProductResponse;
import com.sm.product_service.entities.Product;
import com.sm.product_service.mapper.ProductMapper;
import com.sm.product_service.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	
	private final ProductRepository productRepository;
	
	public ProductResponse saveProduct(ProductRequest request) {
		Product product = ProductMapper.INSTANCE.mapProductRequestToEntity(request);
		productRepository.save(product);
		
		ProductResponse response=ProductMapper.INSTANCE.mapEntityToProductResponse(product);
		return response;
	}

}
