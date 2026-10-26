package com.sm.order_service.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.order_service.dto.request.OrderItemRequest;
import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.request.ReservedStock;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.dto.response.OrderStatusHistoryResponse;
import com.sm.order_service.dto.response.OrderStatusResponse;
import com.sm.order_service.dto.response.PageResponse;
import com.sm.order_service.dto.response.ProductResponseDto;
import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderLineItem;
import com.sm.order_service.entity.OrderStatus;
import com.sm.order_service.entity.OrderStatusHistory;
import com.sm.order_service.exception.InvalidRequestStateException;
import com.sm.order_service.exception.ResourceNotFoundException;
import com.sm.order_service.mapper.OrderMapper;
import com.sm.order_service.repository.OrderRepository;
import com.sm.order_service.repository.OrderStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final ProductServiceClient productServiceClient;
	private final InventoryServiceClient inventoryServiceClient;
	private final OrderStatusHistoryRepository orderStatusHistoryRepository;

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

			// 6. maintain order history
			recordStatusHistory(savedOrder, null, OrderStatus.PENDING, "Order created");

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

	@Transactional(readOnly = true)
	public PageResponse<OrderResponse> getAllOrders(int page, int size, String sortBy, String direction) {

		Set<String> allowedSortFields = Set.of("id", "orderNumber", "userId", "status", "totalAmount", "createdAt",
				"updatedAt");

		if (!allowedSortFields.contains(sortBy)) {
			throw new InvalidRequestStateException(
					"Invalid sortBy field. Allowed fields: " + String.join(", ", allowedSortFields));
		}

		if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
			throw new InvalidRequestStateException("Invalid direction. Allowed values: asc, desc");
		}

		Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Order> orderPage = orderRepository.findAll(pageable);

		List<OrderResponse> orders = orderPage.getContent().stream().map(orderMapper::toOrderResponse).toList();

		return new PageResponse<>(orders, orderPage.getNumber(), orderPage.getSize(), orderPage.getTotalElements(),
				orderPage.getTotalPages(), orderPage.isFirst(), orderPage.isLast());
	}

	@Transactional
	public OrderResponse cancelOrder(Integer id) {

		// 1. Find the order.
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

		// 2. Allow cancellation only for PENDING or CONFIRMED orders.
		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {

			throw new InvalidRequestStateException(
					"Order cannot be cancelled because its status is " + order.getStatus());
		}

		// 3. Release the reserved stock for each line item.
		for (OrderLineItem item : order.getOrderLineItems()) {
			inventoryServiceClient.releaseStock(item.getSkuCode(), item.getQuantity());
		}

		OrderStatus oldStatus = order.getStatus();

		// 4. Update the order status.
		order.setStatus(OrderStatus.CANCELLED);

		// 5. Persist and return the updated order.
		Order cancelledOrder = orderRepository.save(order);

		// 6. maintain order history
		recordStatusHistory(cancelledOrder, oldStatus, OrderStatus.CANCELLED, "Order cancelled");

		return orderMapper.toOrderResponse(cancelledOrder);
	}

	@Transactional
	public OrderResponse updateOrderStatus(Integer id, OrderStatus newStatus) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

		OrderStatus currentStatus = order.getStatus();

		boolean validTransition = switch (currentStatus) {
		case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.FAILED;

		case CONFIRMED -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.FAILED;

		case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.FAILED;

		case SHIPPED -> newStatus == OrderStatus.DELIVERED;

		case DELIVERED, CANCELLED, FAILED -> false;
		};

		if (!validTransition) {
			throw new InvalidRequestStateException(
					"Invalid order status transition from " + currentStatus + " to " + newStatus);
		}

		order.setStatus(newStatus);

		Order updatedOrder = orderRepository.save(order);

		recordStatusHistory(updatedOrder, currentStatus, newStatus, "Order status updated");

		return orderMapper.toOrderResponse(updatedOrder);
	}

	/**
	 * Retrieves the current status of an order by its ID.
	 *
	 * @param id the ID of the order
	 * @return the order ID, order number, and current status
	 * @throws ResourceNotFoundException if the order does not exist
	 */
	@Transactional(readOnly = true)
	public OrderStatusResponse getOrderStatus(Integer id) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

		return new OrderStatusResponse(order.getId(), order.getOrderNumber(), order.getStatus());
	}

	private void recordStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, String remarks) {

		OrderStatusHistory history = new OrderStatusHistory();
		history.setOrderId(order.getId());
		history.setOldStatus(oldStatus);
		history.setNewStatus(newStatus);
		history.setRemarks(remarks);

		orderStatusHistoryRepository.save(history);
	}

	/**
	 * Retrieves the status history of an order in chronological order.
	 *
	 * @param id the ID of the order
	 * @return the list of status changes for the order
	 * @throws ResourceNotFoundException if the order does not exist
	 */
	@Transactional(readOnly = true)
	public List<OrderStatusHistoryResponse> getOrderStatusHistory(Integer id) {

		orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

		return orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAscIdAsc(id).stream()
				.map(history -> new OrderStatusHistoryResponse(history.getOldStatus(), history.getNewStatus(),
						history.getChangedAt(), history.getRemarks()))
				.toList();
	}

	/**
	 * Retrieves an order using its unique order number.
	 *
	 * @param orderNumber the unique order number
	 * @return the order details
	 * @throws ResourceNotFoundException if the order does not exist
	 */
	@Transactional(readOnly = true)
	public OrderResponse getOrderByOrderNumber(String orderNumber) {

		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

		return orderMapper.toOrderResponse(order);
	}

}
