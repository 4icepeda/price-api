package com.inditex.pricing.adapter.in.web;

import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller exposing the price query endpoint.
 * Delegates to the FindApplicablePriceUseCase input port and maps domain results to DTOs.
 */
@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;

    public PriceController(FindApplicablePriceUseCase findApplicablePriceUseCase) {
        this.findApplicablePriceUseCase = findApplicablePriceUseCase;
    }

    @GetMapping
    public ResponseEntity<PriceResponse> findApplicablePrice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam Long productId,
            @RequestParam Long brandId
    ) {
        return findApplicablePriceUseCase.findApplicablePrice(applicationDate, productId, brandId)
                .map(PriceResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
