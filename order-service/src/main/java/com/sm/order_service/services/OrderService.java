package com.sm.order_service.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.response.InventoryResponseDto;
import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderLineItems;
import com.sm.order_service.mapper.OrderMapper;
import com.sm.order_service.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
	
	private final OrderRepository orderRepository;
	private final ExecuteAPIService executeAPIService;

	public String placeOrder(OrderRequest orderRequest) {
		Order order=new Order();
		order.setOrderNumber(UUID.randomUUID().toString());
		List<OrderLineItems> orderLineItems=orderRequest.getOrderLineItems().stream()
				.map(OrderMapper.INSTANCE::mapOrderItemsDtoToEntity).toList();
		order.setOrderLineItems(orderLineItems);
		
		List<String>skuList= orderLineItems.stream().map(OrderLineItems::getSkuCode).toList();
		List<InventoryResponseDto>inventoryResponse=executeAPIService.executeInventoryBySku(skuList);
		
		boolean allProductInStock=inventoryResponse.stream()
				.allMatch(InventoryResponseDto::isInStock);
		
		if(allProductInStock) {
			orderRepository.save(order);
			return "order is place. Order number is "+order.getOrderNumber();
		}else {
			return "order is not place. All products are not in stock";
		}	
		
	}

}
