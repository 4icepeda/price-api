package com.inditex.pricing.application.usecase;

import com.inditex.pricing.application.port.out.PriceMetricsPort;
import com.inditex.pricing.domain.exception.PriorityConflictException;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.out.PriceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FindApplicablePriceService.
 * Mocks the output port (PriceRepositoryPort) to isolate business logic.
 * No Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class FindApplicablePriceServiceTest {

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @Mock
    private PriceMetricsPort metricsPort;

    private FindApplicablePriceService service;

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0, 0);
    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new FindApplicablePriceService(priceRepositoryPort, metricsPort);
    }

    @Test
    @DisplayName("Should return empty when no prices match")
    void shouldReturnEmptyWhenNoPricesMatch() {
        when(priceRepositoryPort.findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(Collections.emptyList());

        Optional<Price> result = service.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).isEmpty();
        verify(priceRepositoryPort).findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);
    }

    @Test
    @DisplayName("Should return the single price when only one matches")
    void shouldReturnSinglePriceWhenOnlyOneMatches() {
        Price price = new Price(1L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                1, PRODUCT_ID, 0, new BigDecimal("35.50"), "EUR");

        when(priceRepositoryPort.findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(List.of(price));

        Optional<Price> result = service.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(1);
        assertThat(result.get().amount()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    @DisplayName("Should return the price with highest priority when multiple match")
    void shouldReturnHighestPriorityPriceWhenMultipleMatch() {
        Price lowPriority = new Price(1L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                1, PRODUCT_ID, 0, new BigDecimal("35.50"), "EUR");

        Price highPriority = new Price(2L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 15, 0, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30, 0),
                2, PRODUCT_ID, 1, new BigDecimal("25.45"), "EUR");

        when(priceRepositoryPort.findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(List.of(lowPriority, highPriority));

        Optional<Price> result = service.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(2);
        assertThat(result.get().priority()).isEqualTo(1);
        assertThat(result.get().amount()).isEqualByComparingTo(new BigDecimal("25.45"));
    }

    @Test
    @DisplayName("Should select highest priority among three overlapping prices")
    void shouldSelectHighestPriorityAmongThreeOverlappingPrices() {
        Price priority0 = new Price(1L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                1, PRODUCT_ID, 0, new BigDecimal("35.50"), "EUR");

        Price priority1 = new Price(2L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 15, 0, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30, 0),
                2, PRODUCT_ID, 1, new BigDecimal("25.45"), "EUR");

        Price priority2 = new Price(3L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 15, 0, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30, 0),
                3, PRODUCT_ID, 2, new BigDecimal("20.00"), "EUR");

        when(priceRepositoryPort.findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(List.of(priority0, priority1, priority2));

        Optional<Price> result = service.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        assertThat(result).isPresent();
        assertThat(result.get().priority()).isEqualTo(2);
        assertThat(result.get().priceList()).isEqualTo(3);
        assertThat(result.get().amount()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Should throw PriorityConflictException when two prices share the same maximum priority")
    void shouldThrowWhenTwoPricesShareTheSameMaxPriority() {
        Price duplicate1 = new Price(1L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                1, PRODUCT_ID, 1, new BigDecimal("35.50"), "EUR");

        Price duplicate2 = new Price(2L, BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                2, PRODUCT_ID, 1, new BigDecimal("25.45"), "EUR");

        when(priceRepositoryPort.findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(List.of(duplicate1, duplicate2));

        assertThatThrownBy(() -> service.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .isInstanceOf(PriorityConflictException.class)
                .hasMessageContaining("Integridad de datos violada")
                .hasMessageContaining("prioridad 1")
                .hasMessageContaining("productId=" + PRODUCT_ID)
                .hasMessageContaining("brandId=" + BRAND_ID);

        verify(metricsPort).recordPriorityConflict(PRODUCT_ID, BRAND_ID, 2);
    }

    @Test
    @DisplayName("Should delegate to repository port with correct parameters")
    void shouldDelegateToRepositoryPortWithCorrectParameters() {
        when(priceRepositoryPort.findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID))
                .thenReturn(Collections.emptyList());

        service.findApplicablePrice(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

        verify(priceRepositoryPort).findApplicablePrices(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);
    }
}
