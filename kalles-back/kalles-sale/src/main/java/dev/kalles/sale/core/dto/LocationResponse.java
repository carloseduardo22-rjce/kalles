package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.entity.Location;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record LocationResponse(

    UUID id,

    @Schema(description = "ID do depósito ao qual esta localização pertence")
    UUID warehouseId,

    @Schema(description = "Nome do depósito")
    String warehouseName,

    @Schema(description = "Código da localização dentro do depósito")
    String code,

    @Schema(description = "Descrição da localização")
    String description
) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(
            location.getId(),
            location.getWarehouse().getId(),
            location.getWarehouse().getName(),
            location.getCode(),
            location.getDescription()
        );
    }
}
