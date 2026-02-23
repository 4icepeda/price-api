package com.inditex.pricing.adapter.out.cache;

import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caching decorator for PriceRepositoryPort.
 * Wraps any PriceRepositoryPort delegate and caches results in memory using a ConcurrentHashMap.
 *
 * <p>The cache key is the combination of (applicationDate, productId, brandId).
 * Results are cached indefinitely for the lifetime of the application instance,
 * which is acceptable given that price tariffs are static data loaded at startup.
 *
 * <p>For production use with mutable data, replace the backing store with a
 * TTL-aware cache (e.g. Caffeine with expireAfterWrite) without changing this class's contract.
 */
public class CachingPriceRepositoryAdapter implements PriceRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(CachingPriceRepositoryAdapter.class);

    private final PriceRepositoryPort delegate;
    private final ConcurrentMap<CacheKey, List<Price>> cache = new ConcurrentHashMap<>();

    public CachingPriceRepositoryAdapter(PriceRepositoryPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Price> findApplicablePrices(LocalDateTime applicationDate, Long productId, Long brandId) {
        CacheKey key = new CacheKey(applicationDate, productId, brandId);
        List<Price> cached = cache.get(key);
        if (cached != null) {
            log.debug("Cache HIT: productId={}, brandId={}, fecha={}", productId, brandId, applicationDate);
            return cached;
        }
        log.debug("Cache MISS: productId={}, brandId={}, fecha={}", productId, brandId, applicationDate);
        List<Price> prices = delegate.findApplicablePrices(applicationDate, productId, brandId);
        cache.put(key, prices);
        return prices;
    }

    private record CacheKey(LocalDateTime applicationDate, Long productId, Long brandId) {}
}
