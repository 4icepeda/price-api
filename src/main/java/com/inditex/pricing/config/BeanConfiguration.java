package com.inditex.pricing.config;

import com.inditex.pricing.adapter.out.cache.CachingPriceRepositoryAdapter;
import com.inditex.pricing.adapter.out.metrics.MicrometerCacheMetricsAdapter;
import com.inditex.pricing.adapter.out.metrics.MicrometerPriceMetricsAdapter;
import com.inditex.pricing.adapter.out.persistence.PricePersistenceAdapter;
import com.inditex.pricing.adapter.out.persistence.SpringDataPriceRepository;
import com.inditex.pricing.application.usecase.FindApplicablePriceService;
import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires the hexagonal architecture together.
 * This is the only place where concrete adapter implementations are bound to port interfaces.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public PriceRepositoryPort priceRepositoryPort(SpringDataPriceRepository springDataPriceRepository,
                                                   MeterRegistry meterRegistry) {
        PriceRepositoryPort persistence = new PricePersistenceAdapter(springDataPriceRepository);
        return new CachingPriceRepositoryAdapter(persistence, new MicrometerCacheMetricsAdapter(meterRegistry));
    }

    @Bean
    public FindApplicablePriceUseCase findApplicablePriceUseCase(PriceRepositoryPort priceRepositoryPort,
                                                                  MeterRegistry meterRegistry) {
        return new FindApplicablePriceService(priceRepositoryPort, new MicrometerPriceMetricsAdapter(meterRegistry));
    }
}
