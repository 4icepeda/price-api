package com.inditex.pricing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a price tariff for a product within a brand.
 * This is a pure domain object with no framework dependencies.
 */
public record Price(
        Long id,
        Long brandId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer priceList,
        Long productId,
        Integer priority,
        BigDecimal amount,
        String currency
) {

    public Price {
        if (brandId == null) throw new IllegalArgumentException("brandId must not be null");
        if (startDate == null) throw new IllegalArgumentException("startDate must not be null");
        if (endDate == null) throw new IllegalArgumentException("endDate must not be null");
        if (priceList == null) throw new IllegalArgumentException("priceList must not be null");
        if (productId == null) throw new IllegalArgumentException("productId must not be null");
        if (priority == null) throw new IllegalArgumentException("priority must not be null");
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency must not be null or blank");
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("startDate must not be after endDate");
    }
}
