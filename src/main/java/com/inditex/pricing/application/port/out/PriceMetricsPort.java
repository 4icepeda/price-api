package com.inditex.pricing.application.port.out;

/**
 * Output port for recording business-level price metrics.
 * Defined in the application layer because metrics are an application concern,
 * not a domain concept. Implementations live in adapter/out/metrics/.
 */
public interface PriceMetricsPort {

    /**
     * Records a data integrity violation where two or more active prices share
     * the same maximum priority for a given product, brand and date combination.
     */
    void recordPriorityConflict(Long productId, Long brandId, int conflictingCount);
}
