package com.inditex.pricing.application.usecase;

import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

/**
 * Application service implementing the FindApplicablePrice use case.
 * Retrieves all matching prices from the repository and selects the one with
 * the highest priority as the disambiguation rule.
 */
public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindApplicablePriceService.class);

    private final PriceRepositoryPort priceRepositoryPort;

    public FindApplicablePriceService(PriceRepositoryPort priceRepositoryPort) {
        this.priceRepositoryPort = priceRepositoryPort;
    }

    @Override
    public Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        log.debug("Buscando precio aplicable: productId={}, brandId={}, fecha={}", productId, brandId, applicationDate);
        var result = priceRepositoryPort.findApplicablePrices(applicationDate, productId, brandId)
                .stream()
                .max(Comparator.comparingInt(Price::priority));
        log.debug("Resultado: {}", result.map(p -> "priceList=" + p.priceList() + " priority=" + p.priority()).orElse("ninguno"));
        return result;
    }
}
