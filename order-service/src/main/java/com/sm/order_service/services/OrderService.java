package com.sm.order_service.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

		log.info("Starting order creation | OrderNumber: {} | UserId: {} | Items: {}", order.getOrderNumber(),
				request.getUserId(), request.getItems().size());

		try {
			for (OrderItemRequest itemRequest : request.getItems()) {

				String skuCode = itemRequest.getSkuCode();

				// 1. Fetch product details.
				ProductResponseDto product = productServiceClient.getProductBySkuCode(skuCode);

				if (product == null || !product.isActive() || product.getSkuCode() == null
						|| !skuCode.equals(product.getSkuCode())) {
					throw new InvalidRequestStateException("Product is unavailable or invalid for SKU: " + skuCode);
				}

				// 2. Validate price.
				if (product.getPrice() == null || product.getPrice().signum() <= 0) {
					throw new InvalidRequestStateException("Invalid product price for SKU: " + skuCode);
				}

				// 3. Reserve stock.
				inventoryServiceClient.reserveStock(skuCode, itemRequest.getQuantity());

				reservedStocks.add(new ReservedStock(skuCode, itemRequest.getQuantity()));

				// BUSINESS LOG: Inventory reservation successful.
				log.info("Inventory reserved successfully | OrderNumber: {} | SKU: {} | Quantity: {}",
						order.getOrderNumber(), skuCode, itemRequest.getQuantity());

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

			// 6. Maintain order history.
			recordStatusHistory(savedOrder, null, OrderStatus.PENDING, "Order created");

			// BUSINESS LOG: Log only after the database transaction commits.
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					log.info(
							"Order created successfully | OrderId: {} | OrderNumber: {} | UserId: {} | TotalAmount: {}",
							savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getUserId(),
							savedOrder.getTotalAmount());
				}
			});

			return orderMapper.toOrderResponse(savedOrder);

		} catch (RuntimeException exception) {

			// BUSINESS LOG: Order creation failed.
			log.warn("Order creation failed | OrderNumber: {} | ReservedItems: {} | Exception: {}",
					order.getOrderNumber(), reservedStocks.size(), exception.getClass().getSimpleName());

			// Release reservations in reverse order.
			for (int i = reservedStocks.size() - 1; i >= 0; i--) {

				ReservedStock reserved = reservedStocks.get(i);

				try {
					String releaseReference = "ORDER-CREATE-" + order.getOrderNumber() + "-RESERVATION-" + i;

					inventoryServiceClient.releaseStock(reserved.skuCode(), reserved.quantity(), releaseReference);

					// BUSINESS LOG: Compensation successful.
					log.info("Stock compensation successful | OrderNumber: {} | SKU: {} | Quantity: {}",
							order.getOrderNumber(), reserved.skuCode(), reserved.quantity());

				} catch (RuntimeException compensationException) {

					log.error("Stock compensation failed | OrderNumber: {} | SKU: {} | Quantity: {}",
							order.getOrderNumber(), reserved.skuCode(), reserved.quantity(), compensationException);

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

	@Transactional
	public OrderResponse cancelOrder(Integer id) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

		// Allow cancellation only for PENDING or CONFIRMED orders.
		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {

			log.warn("Order cancellation rejected | OrderId: {} | CurrentStatus: {}", id, order.getStatus());

			throw new InvalidRequestStateException(
					"Order cannot be cancelled because its status is " + order.getStatus());
		}

		// Release reserved stock for every line item.
		for (OrderLineItem item : order.getOrderLineItems()) {

			String releaseReference = "ORDER-" + order.getId() + "-LINE-" + item.getId();

			inventoryServiceClient.releaseStock(item.getSkuCode(), item.getQuantity(), releaseReference);

			log.info("Inventory released successfully | OrderId: {} | SKU: {} | Quantity: {}", order.getId(),
					item.getSkuCode(), item.getQuantity());
		}

		OrderStatus oldStatus = order.getStatus();

		order.setStatus(OrderStatus.CANCELLED);

		Order cancelledOrder = orderRepository.save(order);

		// Maintain order history.
		recordStatusHistory(cancelledOrder, oldStatus, OrderStatus.CANCELLED, "Order cancelled and inventory released");

		log.info(
				"Order cancelled successfully | OrderId: {} | OrderNumber: {} | PreviousStatus: {} | CurrentStatus: {}",
				cancelledOrder.getId(), cancelledOrder.getOrderNumber(), oldStatus, cancelledOrder.getStatus());

		return orderMapper.toOrderResponse(cancelledOrder);
	}

	@Transactional
	public OrderResponse updateOrderStatus(Integer id, OrderStatus newStatus) {

		if (newStatus == null) {
			throw new InvalidRequestStateException("Order status is required");
		}

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

		OrderStatus currentStatus = order.getStatus();

		// Idempotency: do not repeat an already completed status update.
		if (currentStatus == newStatus) {
			log.info("Order status update skipped | OrderId: {} | Status: {} | Reason: Status already applied", id,
					currentStatus);

			return orderMapper.toOrderResponse(order);
		}

		boolean validTransition = switch (currentStatus) {
		case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.FAILED;
		case CONFIRMED -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.FAILED;
		case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.FAILED;
		case SHIPPED -> newStatus == OrderStatus.DELIVERED;
		case DELIVERED, CANCELLED, FAILED -> false;
		};

		if (!validTransition) {
			log.warn("Order status transition rejected | OrderId: {} | CurrentStatus: {} | RequestedStatus: {}", id,
					currentStatus, newStatus);

			throw new InvalidRequestStateException(
					"Invalid order status transition from " + currentStatus + " to " + newStatus);
		}

		// Release reserved inventory before marking the order as FAILED.
		if (newStatus == OrderStatus.FAILED) {
			for (OrderLineItem item : order.getOrderLineItems()) {

				String releaseReference = "ORDER-" + order.getId() + "-LINE-" + item.getId();

				inventoryServiceClient.releaseStock(item.getSkuCode(), item.getQuantity(), releaseReference);

				log.info("Inventory released successfully | OrderId: {} | SKU: {} | Quantity: {}", order.getId(),
						item.getSkuCode(), item.getQuantity());
			}
		}

		// Update order only after inventory compensation succeeds.
		order.setStatus(newStatus);
		Order updatedOrder = orderRepository.save(order);

		String remarks = newStatus == OrderStatus.FAILED ? "Order failed and inventory released"
				: "Order status updated";

		recordStatusHistory(updatedOrder, currentStatus, newStatus, remarks);

		log.info(
				"Order status updated successfully | OrderId: {} | OrderNumber: {} | PreviousStatus: {} | CurrentStatus: {}",
				updatedOrder.getId(), updatedOrder.getOrderNumber(), currentStatus, updatedOrder.getStatus());

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

	@Transactional(readOnly = true)
	public PageResponse<OrderResponse> getOrders(OrderStatus status, LocalDate fromDate, LocalDate toDate, int page,
			int size, String sortBy, String direction) {

		// Validate date range
		if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
			throw new InvalidRequestStateException("fromDate must be before or equal to toDate");
		}

		// Validate pagination
		if (page < 0) {
			throw new InvalidRequestStateException("Page number cannot be negative");
		}

		if (size < 1) {
			throw new InvalidRequestStateException("Page size must be at least 1");
		}

		// Restrict sorting to valid Order entity properties
		Set<String> allowedSortFields = Set.of("id", "orderNumber", "userId", "status", "totalAmount", "createdAt",
				"updatedAt");

		if (!allowedSortFields.contains(sortBy)) {
			throw new InvalidRequestStateException("Invalid sort field: " + sortBy);
		}

		if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
			throw new InvalidRequestStateException("Direction must be either 'asc' or 'desc'");
		}

		// Convert dates to timestamp boundaries
		LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;

		LocalDateTime endDateTimeExclusive = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;

		// Configure sorting and pagination
		Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(page, size, sort);

		// Fetch orders using all supplied filters
		Page<Order> orders = orderRepository.searchOrders(status, startDateTime, endDateTimeExclusive, pageable);

		// Map entities to response DTOs
		List<OrderResponse> content = orders.getContent().stream().map(orderMapper::toOrderResponse).toList();

		return new PageResponse<>(content, orders.getNumber(), orders.getSize(), orders.getTotalElements(),
				orders.getTotalPages(), orders.hasNext(), orders.hasPrevious());
	}

}
