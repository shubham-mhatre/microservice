package com.sm.payment_service.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sm.payment_service.dto.respositories.PaymentRepository;
import com.sm.payment_service.entity.Payment;
import com.sm.payment_service.enums.OrderSyncStatus;
import com.sm.payment_service.enums.PaymentStatus;
import com.sm.payment_service.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSyncScheduler {

	private final PaymentRepository paymentRepository;
	private final PaymentService paymentService;

	private static final int MAX_SYNC_ATTEMPTS = 5;

	@Scheduled(fixedDelay = 60000)
	public void retryPendingOrderSynchronizations() {

		List<Payment> pendingPayments = paymentRepository
				.findTop50ByStatusAndOrderSyncStatusAndOrderSyncAttemptsLessThanOrderByCreatedAtAsc(
						PaymentStatus.SUCCESS, OrderSyncStatus.PENDING, MAX_SYNC_ATTEMPTS);

		for (Payment payment : pendingPayments) {
			try {
				paymentService.synchronizeOrder(payment.getId());

			} catch (Exception ex) {
				log.error("Order synchronization failed for payment ID: {}", payment.getId(), ex);
			}
		}
	}
}
