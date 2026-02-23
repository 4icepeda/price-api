package com.inditex.pricing.application.usecase;

import com.inditex.pricing.application.port.out.PriceMetricsPort;
import com.inditex.pricing.domain.exception.PriorityConflictException;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import com.inditex.pricing.application.port.out.PriceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service implementing the FindApplicablePrice use case.
 * Retrieves all matching prices from the repository and selects the one with
 * the highest priority as the disambiguation rule.
 */
public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindApplicablePriceService.class);

    private final PriceRepositoryPort priceRepositoryPort;
    private final PriceMetricsPort metricsPort;

    public FindApplicablePriceService(PriceRepositoryPort priceRepositoryPort, PriceMetricsPort metricsPort) {
        this.priceRepositoryPort = priceRepositoryPort;
        this.metricsPort = metricsPort;
    }

    @Override
    public Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        log.debug("Buscando precio aplicable: productId={}, brandId={}, fecha={}", productId, brandId, applicationDate);

        Map<Integer, List<Price>> byPriority = priceRepositoryPort
                .findApplicablePrices(applicationDate, productId, brandId)
                .stream()
                .collect(Collectors.groupingBy(Price::priority));

        if (byPriority.isEmpty()) {
            log.debug("Resultado: ninguno");
            return Optional.empty();
        }

        int maxPriority = byPriority.keySet().stream().max(Comparator.naturalOrder()).orElseThrow();
        List<Price> topPrices = byPriority.get(maxPriority);

        if (topPrices.size() > 1) {
            metricsPort.recordPriorityConflict(productId, brandId, topPrices.size());
            throw new PriorityConflictException(productId, brandId, topPrices.size(), maxPriority);
        }

        Price result = topPrices.get(0);
        log.debug("Resultado: priceList={} priority={}", result.priceList(), result.priority());
        return Optional.of(result);
    }
}
