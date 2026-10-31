package com.sm.inventory_service.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sm.inventory_service.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

	public List<Inventory> findBySkuCodeIn(List<String> skuCode);

	Optional<Inventory> findBySkuCode(String skuCode);

	boolean existsBySkuCode(String skuCode);

	@Modifying
	@Query("""
			UPDATE Inventory i
			SET i.quantity = i.quantity - :quantity
			WHERE i.skuCode = :skuCode
			  AND i.active = true
			  AND i.quantity >= :quantity
			""")
	int reserveStock(@Param("skuCode") String skuCode, @Param("quantity") int quantity);

	@Modifying
	@Query("""
			UPDATE Inventory i
			SET i.quantity = i.quantity + :quantity
			WHERE i.skuCode = :skuCode
			  AND i.quantity IS NOT NULL
			""")
	int releaseStock(@Param("skuCode") String skuCode, @Param("quantity") int quantity);

}
