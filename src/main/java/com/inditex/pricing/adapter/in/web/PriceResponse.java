package com.inditex.pricing.adapter.in.web;

import com.inditex.pricing.domain.model.Price;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for the price query endpoint.
 * Contains only the fields required by the specification:
 * product id, brand id, applicable tariff, application dates, and final price.
 */
@Schema(description = "Precio aplicable devuelto por la API")
public record PriceResponse(

        @Schema(description = "Identificador del producto", example = "35455")
        Long productId,

        @Schema(description = "Identificador de la marca", example = "1")
        Long brandId,

        @Schema(description = "Identificador de la tarifa aplicada", example = "2")
        Integer priceList,

        @Schema(description = "Inicio de la vigencia de la tarifa", example = "2020-06-14T15:00:00")
        LocalDateTime startDate,

        @Schema(description = "Fin de la vigencia de la tarifa", example = "2020-06-14T18:30:00")
        LocalDateTime endDate,

        @Schema(description = "Precio final a aplicar", example = "25.45")
        BigDecimal price,

        @Schema(description = "Moneda ISO 4217", example = "EUR")
        String currency
) {

    public static PriceResponse fromDomain(Price price) {
        return new PriceResponse(
                price.productId(),
                price.brandId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.amount(),
                price.currency()
        );
    }
}
