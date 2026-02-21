package com.inditex.pricing.adapter.out.persistence;

import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistence adapter implementing the output port.
 * Bridges the domain's PriceRepositoryPort with Spring Data JPA.
 */
public class PricePersistenceAdapter implements PriceRepositoryPort {

    private final SpringDataPriceRepository repository;

    public PricePersistenceAdapter(SpringDataPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Long brandId) {
        return repository.findApplicablePrices(applicationDate, productId, brandId)
                .stream()
                .map(PriceEntity::toDomain)
                .toList();
    }
}
