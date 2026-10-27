package com.sm.payment_service.mapper;

import org.mapstruct.Mapper;

import com.sm.payment_service.entity.Payment;
import com.sm.payment_service.dto.response.PaymentResponse;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse mapPaymentToResponse(Payment payment);
}