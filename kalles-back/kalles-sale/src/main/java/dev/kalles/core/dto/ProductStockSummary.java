package dev.kalles.core.dto;

import java.util.UUID;

public record ProductStockSummary(
    UUID productId,
    long totalQuantity
) {}
