package com.sm.order_service.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sm.order_service.dto.response.OrderItemResponse;
import com.sm.order_service.dto.response.OrderResponse;
import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderLineItem;


@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "orderLineItems", target = "items")
    OrderResponse toOrderResponse(Order order);

    List<OrderItemResponse> toOrderItemResponses(
            List<OrderLineItem> orderLineItems);

    OrderItemResponse toOrderItemResponse(OrderLineItem item);
}
