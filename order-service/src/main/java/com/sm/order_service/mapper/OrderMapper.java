package com.sm.order_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.sm.order_service.dto.request.OrderLineItemsDto;
import com.sm.order_service.entity.OrderLineItems;


@Mapper
public interface OrderMapper {

	OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
	
	OrderLineItems mapOrderItemsDtoToEntity(OrderLineItemsDto orderLineItems);
}
