package com.sm.payment_service.dto.request;



import com.sm.payment_service.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be greater than zero")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
