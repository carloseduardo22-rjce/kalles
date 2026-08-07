package dev.kalles.inventory.dto;

import java.util.UUID;

public record ProductLocationStockView(
    UUID warehouseId,
    String warehouseName,
    UUID locationId,
    String locationCode,
    long quantity
) {}
