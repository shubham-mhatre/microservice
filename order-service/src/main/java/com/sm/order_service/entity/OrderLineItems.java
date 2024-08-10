package com.sm.order_service.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="orders_line_items")
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer orderLineItemId;
	
	@Column(name="sku_code")
	private String skuCode;
	
	private BigDecimal price;
	
	private Integer quantity;
}
