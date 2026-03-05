package dev.kalles.sale.core.dto;

import dev.kalles.sale.core.entity.Stock;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record StockResponse(

    UUID id,

    @Schema(description = "ID do produto")
    UUID productId,

    @Schema(description = "Nome do produto")
    String productName,

    @Schema(description = "Código interno do produto")
    String productInternalCode,

    @Schema(description = "ID da localização")
    UUID locationId,

    @Schema(description = "Código da localização")
    String locationCode,

    @Schema(description = "Nome do depósito")
    String warehouseName,

    @Schema(description = "Quantidade disponível nesta localização")
    int quantity
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(
            stock.getId(),
            stock.getProduct().getId(),
            stock.getProduct().getName(),
            stock.getProduct().getInternalCode(),
            stock.getLocation().getId(),
            stock.getLocation().getCode(),
            stock.getLocation().getWarehouse().getName(),
            stock.getQuantity()
        );
    }
}
