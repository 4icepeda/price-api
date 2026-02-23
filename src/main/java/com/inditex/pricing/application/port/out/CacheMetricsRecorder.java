package com.inditex.pricing.application.port.out;

/**
 * Output port for recording cache metrics.
 * Defined in the application layer so that adapters (e.g. CachingPriceRepositoryAdapter)
 * can depend on the abstraction without creating adapter-to-adapter dependencies.
 * Implementations live in adapter/out/cache/.
 */
public interface CacheMetricsRecorder {

    void recordHit(String cacheName);

    void recordMiss(String cacheName);
}
