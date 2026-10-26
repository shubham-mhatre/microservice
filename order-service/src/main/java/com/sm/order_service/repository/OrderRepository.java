package com.sm.order_service.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

	Optional<Order> findByOrderNumber(String orderNumber);

	boolean existsByOrderNumber(String orderNumber);

	Page<Order> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime fromDate, LocalDateTime toDate,
			Pageable pageable);

	@Query("""
			SELECT o FROM Order o
			WHERE (:status IS NULL OR o.status = :status)
			  AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
			  AND (:toDate IS NULL OR o.createdAt < :toDate)
			""")
	Page<Order> searchOrders(@Param("status") OrderStatus status, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate, Pageable pageable);

}
