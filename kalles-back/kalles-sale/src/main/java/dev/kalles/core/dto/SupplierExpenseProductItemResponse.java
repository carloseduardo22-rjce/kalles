package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierExpenseProductItemResponse(
    UUID productId,
    String productName,
    String productInternalCode,
    long totalQuantity,
    BigDecimal averageUnitCost,
    BigDecimal totalCost
) {}
