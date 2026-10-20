package com.sm.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.sm.product_service.dto.request.ProductRequest;
import com.sm.product_service.dto.response.ProductResponse;
import com.sm.product_service.entities.Product;

@Mapper
public interface ProductMapper {

	ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
	
	Product mapProductRequestToEntity(ProductRequest productRequest);
	
	ProductResponse mapEntityToProductResponse(Product product);
	
	@Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "skuCode", ignore = true)
    void updateProductFromRequest(
            ProductRequest request,
            @MappingTarget Product product
    );
}
