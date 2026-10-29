package com.sm.payment_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProcessPaymentRequest(

    @NotBlank(message = "Payment status is required")
    @Pattern(
        regexp = "SUCCESS|FAILED",
        message = "Status must be either SUCCESS or FAILED"
    )
    String status

) {}
