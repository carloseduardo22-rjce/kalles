package dev.kalles.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationRequest(

    @Schema(description = "Código identificador da localização dentro do depósito", example = "Estante A")
    @NotBlank @Size(max = 100)
    String code,

    @Schema(description = "Descrição opcional da localização", example = "Corredor 3, prateleira superior")
    @Size(max = 255)
    String description
) {
    public LocationRequest {
        code = normalizeRequired(code) == null ? null : normalizeRequired(code).toUpperCase();
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
