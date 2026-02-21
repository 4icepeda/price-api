package com.inditex.pricing.domain.port.out;

import com.inditex.pricing.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Output port for retrieving prices from the persistence layer.
 * The domain defines this interface; the persistence adapter implements it.
 */
public interface PriceRepositoryPort {

    /**
     * Finds all prices that apply for the given product, brand, and date.
     * A price applies when the applicationDate falls between startDate and endDate (inclusive).
     *
     * @param applicationDate the date/time to check
     * @param productId       the product identifier
     * @param brandId         the brand identifier
     * @return list of matching prices (may be empty), unordered
     */
    List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Long brandId);
}
