package com.inditex.pricing.application.usecase;

import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

/**
 * Application service implementing the FindApplicablePrice use case.
 * Retrieves all matching prices from the repository and selects the one with
 * the highest priority as the disambiguation rule.
 */
public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    public FindApplicablePriceService(PriceRepositoryPort priceRepositoryPort) {
        this.priceRepositoryPort = priceRepositoryPort;
    }

    @Override
    public Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return priceRepositoryPort.findApplicablePrices(applicationDate, productId, brandId)
                .stream()
                .max(Comparator.comparingInt(Price::priority));
    }
}
