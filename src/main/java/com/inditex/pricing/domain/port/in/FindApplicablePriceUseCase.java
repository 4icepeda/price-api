package com.inditex.pricing.domain.port.in;

import com.inditex.pricing.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Input port for finding the applicable price for a given product, brand, and application date.
 * When multiple tariffs overlap, the one with the highest priority wins.
 */
public interface FindApplicablePriceUseCase {

    /**
     * Finds the applicable price for the given parameters.
     *
     * @param applicationDate the date/time for which the price is requested
     * @param productId       the product identifier
     * @param brandId         the brand identifier
     * @return the applicable price, or empty if no price applies
     */
    Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
