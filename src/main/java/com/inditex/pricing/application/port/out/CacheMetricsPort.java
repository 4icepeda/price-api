package com.inditex.pricing.application.port.out;

/**
 * Output port for recording cache access metrics.
 * Defined in the application layer to avoid leaking infrastructure types
 * (e.g. Caffeine's Cache<K,V>) into adapter internals.
 * Implementations live in adapter/out/metrics/.
 */
public interface CacheMetricsPort {

    void recordHit(String cacheName);

    void recordMiss(String cacheName);
}
