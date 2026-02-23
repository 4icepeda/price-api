package com.inditex.pricing.domain.exception;

public class PriorityConflictException extends RuntimeException {

    public PriorityConflictException(Long productId, Long brandId, int count, int priority) {
        super("Integridad de datos violada: " + count + " precios activos con prioridad " + priority
                + " para productId=" + productId + ", brandId=" + brandId);
    }
}
