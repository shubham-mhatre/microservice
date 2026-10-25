package com.sm.order_service.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.order_service.dto.request.OrderItemRequest;
import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.request.ReservedStock;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.dto.response.ProductResponseDto;
import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderLineItem;
import com.sm.order_service.entity.OrderStatus;
import com.sm.order_service.exception.InvalidRequestStateException;
import com.sm.order_service.exception.ResourceNotFoundException;
import com.sm.order_service.mapper.OrderMapper;
import com.sm.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final ProductServiceClient productServiceClient;
	private final InventoryServiceClient inventoryServiceClient;

	@Transactional
	public OrderResponse createOrder(OrderRequest request) {

		Order order = new Order();
		order.setOrderNumber("ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
		order.setUserId(request.getUserId());
		order.setStatus(OrderStatus.PENDING);
		order.setTotalAmount(BigDecimal.ZERO);

		List<ReservedStock> reservedStocks = new ArrayList<>();
		BigDecimal totalAmount = BigDecimal.ZERO;

		try {
			for (OrderItemRequest itemRequest : request.getItems()) {

				String skuCode = itemRequest.getSkuCode();

				// 1. Fetch product details.
				ProductResponseDto product = productServiceClient.getProductBySkuCode(skuCode);

				if (product == null || !product.isActive() || product.getSkuCode() == null
						|| !skuCode.equals(product.getSkuCode())) {
					throw new InvalidRequestStateException("Product is unavailable or invalid for SKU: " + skuCode);
				}

				// 2. Validate price. This version disallows zero-priced products.
				if (product.getPrice() == null || product.getPrice().signum() <= 0) {
					throw new InvalidRequestStateException("Invalid product price for SKU: " + skuCode);
				}

				// 3. Reserve stock.
				inventoryServiceClient.reserveStock(skuCode, itemRequest.getQuantity());

				reservedStocks.add(new ReservedStock(skuCode, itemRequest.getQuantity()));

				// 4. Build the order line item.
				BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

				OrderLineItem lineItem = new OrderLineItem();
				lineItem.setSkuCode(skuCode);
				lineItem.setProductName(product.getName());
				lineItem.setPrice(product.getPrice());
				lineItem.setQuantity(itemRequest.getQuantity());
				lineItem.setLineTotal(lineTotal);

				order.addOrderLineItem(lineItem);

				totalAmount = totalAmount.add(lineTotal);
			}

			// 5. Set the final total and persist the order.
			order.setTotalAmount(totalAmount);

			Order savedOrder = orderRepository.save(order);

			return orderMapper.toOrderResponse(savedOrder);

		} catch (RuntimeException exception) {

			// Release successful reservations in reverse order.
			Collections.reverse(reservedStocks);

			for (ReservedStock reserved : reservedStocks) {
				try {
					inventoryServiceClient.releaseStock(reserved.skuCode(), reserved.quantity());
				} catch (RuntimeException compensationException) {
					exception.addSuppressed(compensationException);
				}
			}

			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrderById(Integer id) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		return orderMapper.toOrderResponse(order);
	}

}
