
package com.sm.order_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sm.order_service.entity.OrderLineItem;

public interface OrderLineItemRepository extends JpaRepository<OrderLineItem, Integer> {

	List<OrderLineItem> findByOrder_Id(Integer orderId);
}
