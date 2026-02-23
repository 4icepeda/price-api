package com.inditex.pricing.adapter.out.cache;

public interface CacheMetricsPort {

    void recordHit(String cacheName);

    void recordMiss(String cacheName);
}
