package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductCatalogResponse(
    UUID id,
    String name,
    String internalCode,
    String barcode,
    BigDecimal price,
    BigDecimal costPrice,
    String description,
    boolean active,
    int stockQuantity
) {
    public static ProductCatalogResponse from(CompanyProductListItem item, long stock) {
        return new ProductCatalogResponse(
            item.productId(),
            item.name(),
            item.internalCode(),
            item.barcode(),
            item.price(),
            item.costPrice(),
            item.description(),
            item.active(),
            (int) stock
        );
    }
}
