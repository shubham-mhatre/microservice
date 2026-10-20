package com.sm.inventory_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.request.dto.InventoryRequest;

@Mapper
public interface InventoryMapper {

    InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skuCode", source = "skuCode")
    @Mapping(target = "quantity", source = "request.quantity")
    Inventory toEntity(String skuCode, InventoryRequest request);
}
