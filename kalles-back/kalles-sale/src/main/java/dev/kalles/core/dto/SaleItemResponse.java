package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.core.entity.SaleItem;

public record SaleItemResponse(
    UUID id,
    String productName,
    String productInternalCode,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal discount,
    BigDecimal subtotal
) {
    public static SaleItemResponse from(SaleItem item) {
        return new SaleItemResponse(
            item.getId(),
            item.getProduct().getName(),
            item.getProduct().getInternalCode(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getDiscount(),
            item.getSubtotal()
        );
    }
}
