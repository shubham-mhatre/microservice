package com.sm.order_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.order_service.dto.request.OrderRequest;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.entity.Order;
import com.sm.order_service.exception.ResourceNotFoundException;
import com.sm.order_service.mapper.OrderMapper;
import com.sm.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
	
	private final OrderRepository orderRepository;
	private final ExecuteAPIService executeAPIService;
    private final OrderMapper orderMapper;

	public String placeOrder(OrderRequest orderRequest) {
//		Order order=new Order();
//		order.setOrderNumber(UUID.randomUUID().toString());
//		List<OrderLineItem> orderLineItems=orderRequest.getItems().stream()
//				.map(OrderMapper.INSTANCE::mapOrderItemsDtoToEntity).toList();
//		order.setOrderLineItems(orderLineItems);
//		
//		List<String>skuList= orderLineItems.stream().map(OrderLineItem::getSkuCode).toList();
//		List<InventoryResponseDto>inventoryResponse=executeAPIService.executeInventoryBySku1(skuList);
//		
//		boolean allProductInStock=inventoryResponse.stream()
//				.allMatch(InventoryResponseDto::isInStock);
//		
//		if(allProductInStock) {
//			orderRepository.save(order);
//			return "order is place. Order number is "+order.getOrderNumber();
//		}else {
//			return "order is not place. All products are not in stock";
//		}	
		return null;
		
	}
	
	@Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + id));

        return orderMapper.toOrderResponse(order);
    }

}
