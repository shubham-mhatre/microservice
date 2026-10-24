package com.sm.order_service.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {

    private String skuCode;
    private String name;
    private BigDecimal price;
    private boolean active;
}
