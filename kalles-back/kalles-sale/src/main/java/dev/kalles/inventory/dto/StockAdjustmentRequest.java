package dev.kalles.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StockAdjustmentRequest(

    @Schema(description = "ID do produto")
    @NotNull
    UUID productId,

    @Schema(description = "ID da localizacao onde o estoque esta armazenado")
    @NotNull
    UUID locationId,

    @Schema(description = "Quantidade contada nesta localizacao. Substitui a quantidade atual.", example = "97")
    @Min(0)
    int quantity,

    @Schema(description = "Motivo do ajuste, registrado na trilha de auditoria", example = "Contagem de inventario ciclico")
    @NotBlank
    @Size(max = 200)
    String reason
) {}
