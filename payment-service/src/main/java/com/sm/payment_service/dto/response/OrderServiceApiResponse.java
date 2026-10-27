package com.sm.payment_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderServiceApiResponse {

    private boolean success;
    private String message;
    private OrderDetailsResponse data;
}