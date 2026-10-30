package com.sm.payment_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sm.payment_service.enums.OrderSyncStatus;
import com.sm.payment_service.enums.PaymentMethod;
import com.sm.payment_service.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private OrderSyncStatus orderSyncStatus;
    private Integer orderSyncAttempts;
    private String orderSyncError;
    private LocalDateTime orderSyncedAt;
}
