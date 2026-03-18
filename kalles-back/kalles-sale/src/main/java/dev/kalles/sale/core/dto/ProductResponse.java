package dev.kalles.sale.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.sale.core.entity.Product;

public record ProductResponse(
    UUID id,
    String name,
    String internalCode,
    String barcode,
    BigDecimal price,
    String description,
    boolean active,
    Integer stockQuantity,
    String warehouse,
    String location
) {
    public ProductResponse(UUID id, String name, String internalCode, String barcode, BigDecimal price, String description, boolean active, Long stockQuantity, String warehouse, String location) {
        this(id, name, internalCode, barcode, price, description, active, stockQuantity != null ? stockQuantity.intValue() : 0, warehouse, location);
    }

    public static ProductResponse from(Product p) {
        return new ProductResponse(
            p.getId(),
            p.getName(),
            p.getInternalCode(),
            p.getBarcode(),
            p.getPrice(),
            p.getDescription(),
            p.isActive(),
            0,
            null,
            null
        );
    }
}
