package com.sm.order_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.response.ApiResponse;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.services.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;
	
	@GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Integer id) {

		OrderResponse response = orderService.getOrderById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Order fetched successfully", response)
        );
    }
	
	@PostMapping
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
	        @Valid @RequestBody OrderRequest request) {

	    OrderResponse response = orderService.createOrder(request);

	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(ApiResponse.success(
	                    "Order created successfully",
	                    response));
	}
}
