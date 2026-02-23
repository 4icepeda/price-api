package com.inditex.pricing.adapter.out.persistence;

import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.application.port.out.PriceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistence adapter implementing the output port.
 * Bridges the domain's PriceRepositoryPort with Spring Data JPA.
 */
public class PricePersistenceAdapter implements PriceRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(PricePersistenceAdapter.class);

    private final SpringDataPriceRepository repository;

    public PricePersistenceAdapter(SpringDataPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Long brandId) {
        log.debug("Consultando BD: productId={}, brandId={}, fecha={}", productId, brandId, applicationDate);
        var prices = repository.findApplicablePrices(applicationDate, productId, brandId)
                .stream()
                .map(PriceEntity::toDomain)
                .toList();
        log.debug("Filas encontradas en BD: {}", prices.size());
        return prices;
    }
}
