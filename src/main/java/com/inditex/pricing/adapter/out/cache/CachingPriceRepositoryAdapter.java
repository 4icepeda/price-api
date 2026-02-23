package com.inditex.pricing.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caching decorator for PriceRepositoryPort.
 * Wraps any PriceRepositoryPort delegate and caches results in memory using Caffeine.
 *
 * <p>The cache key is the combination of (applicationDate, productId, brandId).
 * Entries expire after 1 hour (expireAfterWrite) and the cache is bounded to 1 000 entries
 * to prevent unbounded memory growth in long-running instances.
 *
 * <p>Price tariffs are static data in this domain, so a 1-hour TTL is conservative.
 * Adjust via BeanConfiguration if business SLAs change.
 */
public class CachingPriceRepositoryAdapter implements PriceRepositoryPort {

    private static final String CACHE_NAME = "prices.repository";

    private final PriceRepositoryPort delegate;
    private final CacheMetricsRecorder cacheMetrics;
    private final Cache<CacheKey, List<Price>> cache;

    public CachingPriceRepositoryAdapter(PriceRepositoryPort delegate, CacheMetricsRecorder cacheMetrics) {
        this.delegate = delegate;
        this.cacheMetrics = cacheMetrics;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Override
    public List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Long brandId) {
        CacheKey key = new CacheKey(applicationDate, productId, brandId);
        List<Price> cached = cache.getIfPresent(key);
        if (cached != null) {
            cacheMetrics.recordHit(CACHE_NAME);
            return cached;
        }
        cacheMetrics.recordMiss(CACHE_NAME);
        List<Price> prices = delegate.findApplicablePrices(applicationDate, productId, brandId);
        cache.put(key, prices);
        return prices;
    }

    private record CacheKey(LocalDateTime applicationDate, Long productId, Long brandId) {}
}
