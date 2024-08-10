package com.sm.order_service.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderLineItems;
import com.sm.order_service.mapper.OrderMapper;
import com.sm.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	
	private final OrderRepository orderRepository;

	public String placeOrder(OrderRequest orderRequest) {
		Order order=new Order();
		order.setOrderNumber(UUID.randomUUID().toString());
		List<OrderLineItems> orderLineItems=orderRequest.getOrderLineItems().stream()
				.map(OrderMapper.INSTANCE::mapOrderItemsDtoToEntity).toList();
		order.setOrderLineItems(orderLineItems);
		
		orderRepository.save(order);
		
		return order.getOrderNumber();
		
	}

}
