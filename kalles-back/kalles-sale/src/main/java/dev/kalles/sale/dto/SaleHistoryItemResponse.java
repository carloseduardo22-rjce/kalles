package dev.kalles.sale.dto;

import dev.kalles.sale.entity.SaleItem;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleHistoryItemResponse(
        UUID id,
        UUID saleId,
        UUID productId,
        String productName,
        String productInternalCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discount,
        BigDecimal subtotal
) {
    public static SaleHistoryItemResponse from(SaleItem item) {
        return new SaleHistoryItemResponse(
                item.getId(),
                item.getSale().getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getInternalCode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscount(),
                item.getSubtotal()
        );
    }
}
