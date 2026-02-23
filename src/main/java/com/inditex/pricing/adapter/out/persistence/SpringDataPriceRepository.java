package com.inditex.pricing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for PriceEntity.
 * The query filters prices where the application date falls within the tariff's date range.
 */
public interface SpringDataPriceRepository extends JpaRepository<PriceEntity, Long> {

    @Transactional(readOnly = true)
    @Query("""
            SELECT p FROM PriceEntity p
            WHERE p.productId = :productId
              AND p.brandId = :brandId
              AND p.startDate <= :applicationDate
              AND p.endDate >= :applicationDate
            """)
    List<PriceEntity> findApplicablePrices(
            @Param("applicationDate") LocalDateTime applicationDate,
            @Param("productId") Long productId,
            @Param("brandId") Long brandId
    );
}
