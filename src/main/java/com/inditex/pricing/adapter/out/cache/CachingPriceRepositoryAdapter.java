package com.inditex.pricing.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inditex.pricing.application.port.out.CacheMetricsRecorder;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.application.port.out.PriceRepositoryPort;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class CachingPriceRepositoryAdapter implements PriceRepositoryPort {

    private static final String CACHE_NAME = "prices.repository";

    private final PriceRepositoryPort delegate;
    private final CacheMetricsRecorder cacheMetrics;
    private final Cache<CacheKey, List<Price>> cache;

    public CachingPriceRepositoryAdapter(PriceRepositoryPort delegate,
                                         CacheMetricsRecorder cacheMetrics,
                                         int maxSize,
                                         Duration ttl) {
        this.delegate = delegate;
        this.cacheMetrics = cacheMetrics;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    @Override
    public List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Long brandId) {
        CacheKey key = new CacheKey(applicationDate, productId, brandId);
        boolean[] wasMiss = {false};
        List<Price> result = cache.get(key, k -> {
            wasMiss[0] = true;
            return delegate.findApplicablePrices(applicationDate, productId, brandId);
        });
        if (wasMiss[0]) {
            cacheMetrics.recordMiss(CACHE_NAME);
        } else {
            cacheMetrics.recordHit(CACHE_NAME);
        }
        return result;
    }

    private record CacheKey(LocalDateTime applicationDate, Long productId, Long brandId) {}
}
