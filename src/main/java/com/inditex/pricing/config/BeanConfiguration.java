package com.inditex.pricing.config;

import com.inditex.pricing.adapter.out.cache.CachingPriceRepositoryAdapter;
import com.inditex.pricing.adapter.out.persistence.PricePersistenceAdapter;
import com.inditex.pricing.adapter.out.persistence.SpringDataPriceRepository;
import com.inditex.pricing.application.usecase.FindApplicablePriceService;
import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires the hexagonal architecture together.
 * This is the only place where concrete adapter implementations are bound to port interfaces.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public PriceRepositoryPort priceRepositoryPort(SpringDataPriceRepository springDataPriceRepository) {
        PriceRepositoryPort persistence = new PricePersistenceAdapter(springDataPriceRepository);
        return new CachingPriceRepositoryAdapter(persistence);
    }

    @Bean
    public FindApplicablePriceUseCase findApplicablePriceUseCase(PriceRepositoryPort priceRepositoryPort) {
        return new FindApplicablePriceService(priceRepositoryPort);
    }
}
