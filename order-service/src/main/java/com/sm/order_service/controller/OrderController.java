package com.sm.order_service.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.request.UpdateOrderStatusRequest;
import com.sm.order_service.dto.response.ApiResponse;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.dto.response.OrderStatusHistoryResponse;
import com.sm.order_service.dto.response.OrderStatusResponse;
import com.sm.order_service.dto.response.PageResponse;
import com.sm.order_service.entity.OrderStatus;
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
			@RequestParam(required = false) OrderStatus status,

			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

			@RequestParam(defaultValue = "0") @Min(0) int page,

			@RequestParam(defaultValue = "10") @Min(1) int size,

			@RequestParam(defaultValue = "createdAt") String sortBy,

			@RequestParam(defaultValue = "desc") String direction) {

		PageResponse<OrderResponse> response = orderService.getOrders(status, fromDate, toDate, page, size, sortBy,
				direction);

		return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", response));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Integer id) {

		OrderResponse response = orderService.cancelOrder(id);

		return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(@PathVariable Integer id,
			@Valid @RequestBody UpdateOrderStatusRequest request) {

		OrderResponse response = orderService.updateOrderStatus(id, request.status());

		return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
	}

	/**
	 * Retrieves the current status of an order.
	 *
	 * @param id the ID of the order
	 * @return the current order status
	 */
	@GetMapping("/{id}/status")
	public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(@PathVariable Integer id) {

		OrderStatusResponse response = orderService.getOrderStatus(id);

		return ResponseEntity.ok(ApiResponse.success("Order status fetched successfully", response));
	}

	/**
	 * Retrieves the status history of an order.
	 *
	 * @param id the ID of the order
	 * @return the order's status history
	 */
	@GetMapping("/{id}/history")
	public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getOrderStatusHistory(
			@PathVariable Integer id) {

		List<OrderStatusHistoryResponse> history = orderService.getOrderStatusHistory(id);

		return ResponseEntity.ok(ApiResponse.success("Order status history fetched successfully", history));
	}

	/**
	 * Retrieves an order using its unique order number.
	 *
	 * @param orderNumber the unique order number
	 * @return the order details
	 */
	@GetMapping("/number/{orderNumber}")
	public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {

		OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);

		return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
	}

}
