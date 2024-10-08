package com.sm.inventory_service.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sm.inventory_service.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer>{

	public List<Inventory> findBySkuCodeIn(List<String> skuCode);
}
