package com.sm.order_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sm.order_service.entity.Order;
import com.sm.order_service.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

	Optional<Order> findByOrderNumber(String orderNumber);

	boolean existsByOrderNumber(String orderNumber);

	Page<Order> findByStatus(OrderStatus status, Pageable pageable);

}
