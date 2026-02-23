package com.inditex.pricing.config;

import com.inditex.pricing.adapter.out.cache.CacheMetricsRecorder;
import com.inditex.pricing.adapter.out.cache.CachingPriceRepositoryAdapter;
import com.inditex.pricing.adapter.out.cache.MicrometerCacheMetricsAdapter;
import com.inditex.pricing.adapter.out.metrics.MicrometerPriceMetricsAdapter;
import com.inditex.pricing.adapter.out.persistence.PricePersistenceAdapter;
import com.inditex.pricing.adapter.out.persistence.SpringDataPriceRepository;
import com.inditex.pricing.application.port.out.PriceMetricsPort;
import com.inditex.pricing.application.usecase.FindApplicablePriceService;
import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Spring configuration that wires the hexagonal architecture together.
 * This is the only place where concrete adapter implementations are bound to port interfaces.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public PriceMetricsPort priceMetricsPort(MeterRegistry meterRegistry) {
        return new MicrometerPriceMetricsAdapter(meterRegistry);
    }

    @Bean
    public CacheMetricsRecorder cacheMetricsRecorder(MeterRegistry meterRegistry) {
        return new MicrometerCacheMetricsAdapter(meterRegistry);
    }

    @Bean
    public PriceRepositoryPort priceRepositoryPort(
            SpringDataPriceRepository springDataPriceRepository,
            CacheMetricsRecorder cacheMetricsRecorder,
            @Value("${pricing.cache.prices.max-size:1000}") int cacheMaxSize,
            @Value("${pricing.cache.prices.ttl-hours:1}") int cacheTtlHours) {
        PriceRepositoryPort persistence = new PricePersistenceAdapter(springDataPriceRepository);
        return new CachingPriceRepositoryAdapter(
                persistence,
                cacheMetricsRecorder,
                cacheMaxSize,
                Duration.ofHours(cacheTtlHours));
    }

    @Bean
    public FindApplicablePriceUseCase findApplicablePriceUseCase(PriceRepositoryPort priceRepositoryPort,
                                                                  PriceMetricsPort priceMetricsPort) {
        return new FindApplicablePriceService(priceRepositoryPort, priceMetricsPort);
    }
}
