package dev.kalles.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(

    @Schema(description = "Nome do depósito", example = "Depósito Central")
    @NotBlank @Size(max = 150)
    String name,

    @Schema(description = "Endereço físico do depósito", example = "Rua das Flores, 100")
    @Size(max = 255)
    String address
) {
    public WarehouseRequest {
        name = normalizeRequired(name);
        address = normalizeOptional(address);
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
