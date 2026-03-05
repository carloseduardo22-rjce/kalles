package dev.kalles.sale.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockRequest(

    @Schema(description = "ID do produto")
    @NotNull
    UUID productId,

    @Schema(description = "ID da localização onde o estoque está armazenado")
    @NotNull
    UUID locationId,

    @Schema(description = "Quantidade do produto nesta localização. Deve ser maior ou igual a zero.", example = "100")
    @Min(0)
    int quantity
) {}
