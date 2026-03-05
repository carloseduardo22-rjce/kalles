package dev.kalles.sale.core.dto;

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
) {}
