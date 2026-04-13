package dev.kalles.sale.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

    @Schema(description = "Nome do produto", example = "Cafe Expresso")
    @NotBlank @Size(max = 150)
    String name,

    @Schema(description = "Codigo interno unico do produto", example = "CAFE-001")
    @NotBlank @Size(max = 50)
    String internalCode,

    @Schema(description = "Codigo de barras", example = "7891234560017")
    @Size(max = 50)
    String barcode,

    @Schema(description = "Descricao detalhada do produto")
    String description,

    @Schema(description = "Preco unitario de venda", example = "5.90")
    @NotNull @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero")
    BigDecimal price,

    @Schema(description = "Custo unitario da mercadoria", example = "3.40")
    @NotNull @DecimalMin(value = "0.01", message = "Custo deve ser maior que zero")
    BigDecimal costPrice
) {
    public ProductRequest {
        name = normalizeRequired(name);
        internalCode = normalizeRequired(internalCode).toUpperCase();
        barcode = normalizeOptional(barcode);
        description = normalizeOptional(description);
    }

    private static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
