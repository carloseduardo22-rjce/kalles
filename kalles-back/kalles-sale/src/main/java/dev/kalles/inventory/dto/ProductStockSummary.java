package dev.kalles.inventory.dto;

import java.util.UUID;

public record ProductStockSummary(
    UUID productId,
    long totalQuantity
) {}
