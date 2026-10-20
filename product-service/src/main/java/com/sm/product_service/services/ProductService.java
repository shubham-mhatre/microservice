package com.sm.product_service.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sm.product_service.dto.request.ProductRequest;
import com.sm.product_service.dto.response.ApiResponse;
import com.sm.product_service.dto.response.PageResponse;
import com.sm.product_service.dto.response.ProductResponse;
import com.sm.product_service.entities.Product;
import com.sm.product_service.mapper.ProductMapper;
import com.sm.product_service.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	
	private final ProductRepository productRepository;
	private final InventoryServiceClient inventoryServiceClient;
	
	public ApiResponse<ProductResponse> saveProduct(ProductRequest request) {
		Product product = ProductMapper.INSTANCE.mapProductRequestToEntity(request);
		Product savedProduct = productRepository.save(product);
		
		inventoryServiceClient.createInventory(savedProduct.getSkuCode());
		
		ProductResponse productResponse =
	            ProductMapper.INSTANCE.mapEntityToProductResponse(savedProduct);

	    return new ApiResponse<>(
	            true,
	            "Product created successfully",
	            productResponse,
	            null
	    );
	}

	public ApiResponse<PageResponse<ProductResponse>> getAll(
	        int page, int size) {

	    Pageable pageable = PageRequest.of(page, size);

	    Page<Product> productPage = productRepository.findAll(pageable);

	    List<ProductResponse> products = productPage.getContent()
	            .stream()
	            .map(ProductMapper.INSTANCE::mapEntityToProductResponse)
	            .toList();

	    PageResponse<ProductResponse> pageResponse =
	            new PageResponse<>(
	                    products,
	                    productPage.getNumber(),
	                    productPage.getSize(),
	                    productPage.getTotalElements(),
	                    productPage.getTotalPages(),
	                    productPage.hasNext(),
	                    productPage.hasPrevious()
	            );

	    return ApiResponse.success(
	            "Products retrieved successfully",
	            pageResponse
	    );
	}

}
