package com.sm.order_service.dto.response;

import com.sm.order_service.entity.OrderStatus;

public record OrderStatusResponse(
        Integer orderId,
        String orderNumber,
        OrderStatus status
) {
}
