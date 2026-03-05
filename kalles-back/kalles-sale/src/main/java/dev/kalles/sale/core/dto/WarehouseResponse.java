package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.entity.Warehouse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record WarehouseResponse(

    UUID id,

    @Schema(description = "Nome do depósito")
    String name,

    @Schema(description = "Endereço físico do depósito")
    String address,

    @Schema(description = "Indica se o depósito está ativo")
    boolean active
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
            warehouse.getId(),
            warehouse.getName(),
            warehouse.getAddress(),
            warehouse.isActive()
        );
    }
}
