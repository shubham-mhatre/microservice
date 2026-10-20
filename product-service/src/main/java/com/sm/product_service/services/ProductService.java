package com.sm.product_service.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.product_service.dto.request.ProductRequest;
import com.sm.product_service.dto.response.ApiResponse;
import com.sm.product_service.dto.response.PageResponse;
import com.sm.product_service.dto.response.ProductResponse;
import com.sm.product_service.entities.Product;
import com.sm.product_service.exception.ResourceNotFoundException;
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
	
	public ApiResponse<ProductResponse> getProductById(Long id) {

	    Product product = productRepository.findById(id)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "Product not found with ID: " + id
	                    )
	            );

	    ProductResponse response =
	            ProductMapper.INSTANCE.mapEntityToProductResponse(product);

	    return new ApiResponse<>(
	            true,
	            "Product fetched successfully",
	            response,
	            null
	    );
	}
	
	public ApiResponse<ProductResponse> getProductBySkuCode(String skuCode) {

	    Product product = productRepository.findBySkuCode(skuCode)
	            .orElseThrow(() -> new ResourceNotFoundException(
	                    "Product not found with SKU: " + skuCode
	            ));

	    ProductResponse response =
	            ProductMapper.INSTANCE.mapEntityToProductResponse(product);

	    return new ApiResponse<>(
	            true,
	            "Product fetched successfully",
	            response,
	            null
	    );
	}
	
	@Transactional
	public ApiResponse<ProductResponse> updateProduct(
	        Long id, ProductRequest request) {

	    Product product = productRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException(
	                    "Product not found with ID: " + id
	            ));

	    ProductMapper.INSTANCE.updateProductFromRequest(request, product);

	    Product updatedProduct = productRepository.save(product);

	    ProductResponse response =
	            ProductMapper.INSTANCE.mapEntityToProductResponse(updatedProduct);

	    return new ApiResponse<>(
	            true,
	            "Product updated successfully",
	            response,
	            null
	    );
	}

}
