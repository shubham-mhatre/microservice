package com.sm.payment_service.dto.respositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sm.payment_service.entity.Payment;
import com.sm.payment_service.enums.OrderSyncStatus;
import com.sm.payment_service.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByOrderIdAndStatusIn(Long orderId, Collection<PaymentStatus> statuses);

	List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

	List<Payment> findTop50ByStatusInAndOrderSyncStatusAndOrderSyncAttemptsLessThanOrderByCreatedAtAsc(
			List<PaymentStatus> statuses, OrderSyncStatus syncStatus, Integer maxAttempts);
}
