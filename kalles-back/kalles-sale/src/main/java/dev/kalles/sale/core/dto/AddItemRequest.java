package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.enums.product.ProductCodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddItemRequest(

    @Schema(description = "Tipo do código do produto", allowableValues = {"INTERNAL_CODE", "BAR_CODE"}, example = "INTERNAL_CODE")
    @NotNull
    ProductCodeType type,

    @Schema(description = "Código do produto (interno ou de barras, conforme o tipo informado)", example = "PRD-001")
    @NotBlank
    String code
) {}
