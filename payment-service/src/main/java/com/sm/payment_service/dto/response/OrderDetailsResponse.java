package com.sm.payment_service.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetailsResponse {

    private Long id;
    private String status;
    private BigDecimal totalAmount;
}