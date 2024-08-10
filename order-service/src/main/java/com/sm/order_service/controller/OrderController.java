package com.sm.order_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.services.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;

	@PostMapping("/place")
	public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
		String orderNumber= orderService.placeOrder(orderRequest);
		return ResponseEntity.ok("order is placed. Order number is "+orderNumber);
	}
}
