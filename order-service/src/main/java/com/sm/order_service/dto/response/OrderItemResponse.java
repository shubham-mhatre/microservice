package com.sm.order_service.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {

	private Integer id;
    private String skuCode;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;
}
