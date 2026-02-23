package com.inditex.pricing.adapter.out.metrics;

import com.inditex.pricing.application.port.out.PriceMetricsPort;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Micrometer implementation of PriceMetricsPort.
 * Records business-level price metrics as Prometheus-compatible counters.
 */
public class MicrometerPriceMetricsAdapter implements PriceMetricsPort {

    private final MeterRegistry registry;

    public MicrometerPriceMetricsAdapter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void recordPriorityConflict(Long productId, Long brandId, int conflictingCount) {
        registry.counter("prices.priority.conflicts").increment();
    }
}
