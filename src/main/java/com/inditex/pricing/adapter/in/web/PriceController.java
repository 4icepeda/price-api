package com.inditex.pricing.adapter.in.web;

import com.inditex.pricing.domain.port.in.FindApplicablePriceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
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
@Tag(name = "Precios", description = "Consulta del precio aplicable para un producto y marca en una fecha dada")
public class PriceController {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;

    public PriceController(FindApplicablePriceUseCase findApplicablePriceUseCase) {
        this.findApplicablePriceUseCase = findApplicablePriceUseCase;
    }

    @Operation(
            summary = "Obtener precio aplicable",
            description = """
                    Devuelve el precio vigente para un producto y marca en la fecha indicada.
                    Si existen varias tarifas solapadas, se aplica la de mayor prioridad.
                    Devuelve 404 si no existe ninguna tarifa vigente para los parametros dados.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Precio aplicable encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametro ausente o con formato invalido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe precio aplicable para los parametros dados",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<PriceResponse> findApplicablePrice(
            @Parameter(description = "Fecha y hora de aplicacion en formato ISO 8601", example = "2020-06-14T16:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,

            @Parameter(description = "Identificador del producto", example = "35455", required = true)
            @RequestParam Long productId,

            @Parameter(description = "Identificador de la marca (1 = ZARA)", example = "1", required = true)
            @RequestParam Long brandId
    ) {
        return findApplicablePriceUseCase.findApplicablePrice(applicationDate, productId, brandId)
                .map(PriceResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
