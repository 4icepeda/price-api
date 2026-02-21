package com.inditex.pricing.adapter.in.web;

import com.inditex.pricing.domain.model.Price;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for the price query endpoint.
 * Contains only the fields required by the specification:
 * product id, brand id, applicable tariff, application dates, and final price.
 */
public record PriceResponse(
        Long productId,
        Long brandId,
        Integer priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency
) {

    public static PriceResponse fromDomain(Price price) {
        return new PriceResponse(
                price.productId(),
                price.brandId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.amount(),
                price.currency()
        );
    }
}
