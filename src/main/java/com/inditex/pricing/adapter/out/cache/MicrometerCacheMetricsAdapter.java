package com.inditex.pricing.adapter.out.cache;

import io.micrometer.core.instrument.MeterRegistry;

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
