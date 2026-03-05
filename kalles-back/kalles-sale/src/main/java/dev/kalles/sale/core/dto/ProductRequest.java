package dev.kalles.sale.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

    @Schema(description = "Nome do produto", example = "Café Expresso")
    @NotBlank @Size(max = 150)
    String name,

    @Schema(description = "Código interno único do produto", example = "CAFE-001")
    @NotBlank @Size(max = 50)
    String internalCode,

    @Schema(description = "Código de barras", example = "7891234560017")
    @Size(max = 50)
    String barcode,

    @Schema(description = "Descrição detalhada do produto")
    String description,

    @Schema(description = "Preço unitário de venda", example = "5.90")
    @NotNull @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    BigDecimal price
) {}
