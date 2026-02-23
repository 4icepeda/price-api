package com.inditex.pricing.adapter.out.cache;

import com.inditex.pricing.application.port.out.CacheMetricsRecorder;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.application.port.out.PriceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingPriceRepositoryAdapterTest {

    @Mock
    private PriceRepositoryPort delegate;

    @Mock
    private CacheMetricsRecorder cacheMetrics;

    private CachingPriceRepositoryAdapter adapter;

    private static final LocalDateTime DATE = LocalDateTime.of(2020, 6, 14, 16, 0, 0);
    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L;

    private static final Price PRICE = new Price(
            1L, BRAND_ID,
            LocalDateTime.of(2020, 6, 14, 0, 0, 0),
            LocalDateTime.of(2020, 12, 31, 23, 59, 59),
            1, PRODUCT_ID, 0, new BigDecimal("35.50"), "EUR"
    );

    @BeforeEach
    void setUp() {
        adapter = new CachingPriceRepositoryAdapter(delegate, cacheMetrics, 1000, Duration.ofHours(1));
    }

    @Test
    @DisplayName("Should call delegate on first request (cache miss)")
    void shouldCallDelegateOnCacheMiss() {
        when(delegate.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID)).thenReturn(List.of(PRICE));

        List<Price> result = adapter.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).containsExactly(PRICE);
        verify(delegate, times(1)).findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
        verify(cacheMetrics).recordMiss("prices.repository");
    }

    @Test
    @DisplayName("Should return cached result and not call delegate on second request (cache hit)")
    void shouldReturnCachedResultOnCacheHit() {
        when(delegate.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID)).thenReturn(List.of(PRICE));

        adapter.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
        List<Price> secondResult = adapter.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);

        assertThat(secondResult).containsExactly(PRICE);
        verify(delegate, times(1)).findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
        verify(cacheMetrics).recordHit("prices.repository");
    }

    @Test
    @DisplayName("Should call delegate separately for different cache keys")
    void shouldCallDelegateForEachDistinctKey() {
        LocalDateTime otherDate = LocalDateTime.of(2020, 6, 15, 10, 0, 0);
        when(delegate.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID)).thenReturn(List.of(PRICE));
        when(delegate.findApplicablePrices(otherDate, PRODUCT_ID, BRAND_ID)).thenReturn(List.of());

        adapter.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
        adapter.findApplicablePrices(otherDate, PRODUCT_ID, BRAND_ID);

        verify(delegate, times(1)).findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
        verify(delegate, times(1)).findApplicablePrices(otherDate, PRODUCT_ID, BRAND_ID);
    }

    @Test
    @DisplayName("Should cache empty result and not call delegate again")
    void shouldCacheEmptyResult() {
        when(delegate.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID)).thenReturn(List.of());

        adapter.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
        adapter.findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);

        verify(delegate, times(1)).findApplicablePrices(DATE, PRODUCT_ID, BRAND_ID);
    }
}
