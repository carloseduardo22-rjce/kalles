package dev.kalles.sale.dto;

import dev.kalles.product.enums.ProductCodeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemRequest(

    @Schema(description = "Tipo do código do produto", allowableValues = {"INTERNAL_CODE", "BAR_CODE"}, example = "INTERNAL_CODE")
    @NotNull
    ProductCodeType type,

    @Schema(description = "Código do produto (interno ou de barras, conforme o tipo informado)", example = "PRD-001")
    @NotBlank
    String code,

    @Schema(description = "Quantidade do item a ser adicionada", example = "4", minimum = "1")
    @NotNull
    @Positive
    Integer quantity
) {}
