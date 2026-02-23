package com.inditex.pricing.adapter.out.cache;

public interface CacheMetricsRecorder {

    void recordHit(String cacheName);

    void recordMiss(String cacheName);
}
