package com.sm.inventory_service.entity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sm.inventory_service.entity.StockRelease;

@Repository
public interface StockReleaseRepository extends JpaRepository<StockRelease, Long> {

    Optional<StockRelease> findByReleaseReference(String releaseReference);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO inventory_stock_release
                (release_reference, sku_code, quantity)
            VALUES
                (:releaseReference, :skuCode, :quantity)
            """, nativeQuery = true)
    int insertIfNotExists(
            @Param("releaseReference") String releaseReference,
            @Param("skuCode") String skuCode,
            @Param("quantity") Integer quantity);
}