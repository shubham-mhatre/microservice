package com.sm.product_service.services;

import java.util.List;

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

		return ProductMapper.INSTANCE.mapEntityToProductResponse(product);
	}

	public List<ProductResponse> getAll() {
		List<Product> allProductList= productRepository.findAll();
		
		return allProductList.stream()
				.map(data->ProductMapper.INSTANCE.mapEntityToProductResponse(data))
				.toList();
	}

}
