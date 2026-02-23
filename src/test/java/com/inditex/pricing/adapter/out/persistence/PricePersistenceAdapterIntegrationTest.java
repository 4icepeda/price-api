package com.inditex.pricing.adapter.out.persistence;

import com.inditex.pricing.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PricePersistenceAdapter.
 * Uses @DataJpaTest with H2 and Flyway migrations to test actual SQL queries
 * and the mapping between JPA entities and domain models.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PricePersistenceAdapterIntegrationTest {

    @Autowired
    private SpringDataPriceRepository springDataPriceRepository;

    private PricePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PricePersistenceAdapter(springDataPriceRepository);
    }

    @Test
    @DisplayName("Should find two applicable prices at 16:00 on June 14 (overlapping tariffs)")
    void shouldFindTwoApplicablePricesForOverlappingTariffs() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 35455L, 1L);

        assertThat(prices).hasSize(2);
        assertThat(prices).extracting(Price::priceList).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("Should find one applicable price at 10:00 on June 14 (only base tariff)")
    void shouldFindOneApplicablePriceForBaseTariff() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 35455L, 1L);

        assertThat(prices).hasSize(1);
        assertThat(prices.get(0).priceList()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find no prices for non-existent product")
    void shouldFindNoPricesForNonExistentProduct() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 99999L, 1L);

        assertThat(prices).isEmpty();
    }

    @Test
    @DisplayName("Should find no prices for non-existent brand")
    void shouldFindNoPricesForNonExistentBrand() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 35455L, 99L);

        assertThat(prices).isEmpty();
    }

    @Test
    @DisplayName("Should find no prices for date outside all tariff ranges")
    void shouldFindNoPricesForDateOutsideAllRanges() {
        LocalDateTime applicationDate = LocalDateTime.of(2019, 1, 1, 0, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 35455L, 1L);

        assertThat(prices).isEmpty();
    }

    @Test
    @DisplayName("Should correctly map JPA entity fields to domain model")
    void shouldCorrectlyMapEntityToDomain() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 35455L, 1L);

        assertThat(prices).hasSize(1);
        Price price = prices.get(0);
        assertThat(price.brandId()).isEqualTo(1L);
        assertThat(price.productId()).isEqualTo(35455L);
        assertThat(price.priceList()).isEqualTo(1);
        assertThat(price.priority()).isEqualTo(0);
        assertThat(price.amount()).isEqualByComparingTo("35.50");
        assertThat(price.currency()).isEqualTo("EUR");
        assertThat(price.startDate()).isEqualTo(LocalDateTime.of(2020, 6, 14, 0, 0, 0));
        assertThat(price.endDate()).isEqualTo(LocalDateTime.of(2020, 12, 31, 23, 59, 59));
    }

    @Test
    @DisplayName("Should find two applicable prices at 10:00 on June 15 (base + morning tariff)")
    void shouldFindTwoPricesForJune15Morning() {
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 15, 10, 0, 0);

        List<Price> prices = adapter.findApplicablePrices(applicationDate, 35455L, 1L);

        assertThat(prices).hasSize(2);
        assertThat(prices).extracting(Price::priceList).containsExactlyInAnyOrder(1, 3);
    }
}
