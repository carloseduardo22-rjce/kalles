package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierExpenseProductSummary(
    UUID productId,
    String productName,
    String productInternalCode,
    Long totalQuantity,
    BigDecimal totalCost
) {}
