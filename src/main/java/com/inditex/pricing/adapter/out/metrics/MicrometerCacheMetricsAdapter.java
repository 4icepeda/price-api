package com.inditex.pricing.adapter.out.metrics;

import com.inditex.pricing.application.port.out.CacheMetricsPort;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Micrometer implementation of CacheMetricsPort.
 * Uses the same tag format as CaffeineCacheMetrics (cache + result) for
 * compatibility with standard Grafana Caffeine dashboards.
 */
public class MicrometerCacheMetricsAdapter implements CacheMetricsPort {

    private final MeterRegistry registry;

    public MicrometerCacheMetricsAdapter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void recordHit(String cacheName) {
        registry.counter("cache.gets", "cache", cacheName, "result", "hit").increment();
    }

    @Override
    public void recordMiss(String cacheName) {
        registry.counter("cache.gets", "cache", cacheName, "result", "miss").increment();
    }
}
