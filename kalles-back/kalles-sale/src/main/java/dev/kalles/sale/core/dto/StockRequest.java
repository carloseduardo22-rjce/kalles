package dev.kalles.sale.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record StockRequest(

    @Schema(description = "ID do produto")
    @NotNull
    UUID productId,

    @Schema(description = "ID da localizacao onde o estoque esta armazenado")
    @NotNull
    UUID locationId,

    @Schema(description = "Quantidade total do produto nesta localizacao. Deve ser maior ou igual a zero.", example = "100")
    @Min(0)
    int quantity,

    @Schema(description = "Custo unitario da mercadoria para registrar novas entradas", example = "12.50")
    @DecimalMin(value = "0.01", message = "Custo deve ser maior que zero")
    BigDecimal unitCost
) {}
