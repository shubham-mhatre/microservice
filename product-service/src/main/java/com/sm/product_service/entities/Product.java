package com.sm.product_service.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long productId;
	
	private String name;
	
	private String description;
	
	private BigDecimal price;
	
	@Column(name = "sku_code")
	private String skuCode;
	
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
	    if (createdAt == null) {
	        createdAt = LocalDateTime.now();
	    }
	}
	
	@Column(nullable = false)
	private boolean active = true;

	
	
}
