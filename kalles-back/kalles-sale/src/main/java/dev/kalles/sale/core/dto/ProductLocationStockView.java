package dev.kalles.sale.core.dto;

import java.util.UUID;

public record ProductLocationStockView(
    UUID warehouseId,
    String warehouseName,
    UUID locationId,
    String locationCode,
    long quantity
) {}
