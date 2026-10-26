package com.sm.order_service.dto.response;

import java.time.LocalDateTime;

import com.sm.order_service.entity.OrderStatus;

public record OrderStatusHistoryResponse(
        OrderStatus oldStatus,
        OrderStatus newStatus,
        LocalDateTime changedAt,
        String remarks
) {
}