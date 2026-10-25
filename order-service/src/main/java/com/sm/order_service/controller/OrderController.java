package com.sm.order_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.response.ApiResponse;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.dto.response.PageResponse;
import com.sm.order_service.services.OrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable @Positive Integer id) {

		OrderResponse response = orderService.getOrderById(id);

		return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {

		OrderResponse response = orderService.createOrder(request);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Order created successfully", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
			@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		PageResponse<OrderResponse> response = orderService.getAllOrders(page, size, sortBy, direction);

		return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", response));
	}
}
