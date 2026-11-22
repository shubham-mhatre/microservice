package com.sm.product_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.product_service.dto.request.ProductRequest;
import com.sm.product_service.dto.response.ApiResponse;
import com.sm.product_service.dto.response.PageResponse;
import com.sm.product_service.dto.response.ProductResponse;
import com.sm.product_service.services.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class Productcontroller {

	private final ProductService productService;

	/**
     * Creates a new product.
     *
     * @param request product details
     * @return created product details
     */
    @PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<ApiResponse<ProductResponse>> saveProduct(@RequestBody ProductRequest request) {
		ApiResponse<ProductResponse> productResponse = productService.saveProduct(request);
		return ResponseEntity.ok(productResponse);
	}

	 /**
     * Retrieves all products with pagination.
     *
     * @param page zero-based page number
     * @param size number of records per page
     * @return paginated product list
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		ApiResponse<PageResponse<ProductResponse>> productList = productService.getAll(page, size);
		return ResponseEntity.ok(productList);
	}
	
	/**
     * Retrieves a product using its ID.
     *
     * @param id product ID
     * @return product details
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {

		ApiResponse<ProductResponse> response = productService.getProductById(id);

		return ResponseEntity.ok(response);
	}
	
    /**
     * Retrieves a product using its SKU code.
     *
     * @param skuCode product SKU code
     * @return product details
     */
    @PreAuthorize("hasAuthority('ROLE_SERVICE_ORDER')")
	@GetMapping("/sku/{skuCode}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProductBySkuCode(
	        @PathVariable String skuCode) {

	    ApiResponse<ProductResponse> response =
	            productService.getProductBySkuCode(skuCode);

	    return ResponseEntity.ok(response);
	}
	
    /**
     * Updates an existing product.
     *
     * @param id product ID
     * @param request updated product details
     * @return updated product details
     */
    @PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
	        @PathVariable Long id,
	        @RequestBody ProductRequest request) {

	    ApiResponse<ProductResponse> response =
	            productService.updateProduct(id, request);

	    return ResponseEntity.ok(response);
	}
	
    /**
     * Deactivates an existing product.
     *
     * @param id product ID
     * @return operation result
     */
    @PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(
	        @PathVariable Long id) {

	    ApiResponse<Void> response = productService.deleteProduct(id);

	    return ResponseEntity.ok(response);
	}
}
