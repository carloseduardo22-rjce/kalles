package dev.kalles.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.core.entity.Product;

public record ProductResponse(
    UUID id,
    String name,
    String internalCode,
    String barcode,
    BigDecimal price,
    BigDecimal costPrice,
    String description,
    boolean active,
    Integer stockQuantity,
    String warehouse,
    String location
) {
    public ProductResponse(UUID id, String name, String internalCode, String barcode, BigDecimal price, BigDecimal costPrice, String description, boolean active, Long stockQuantity, String warehouse, String location) {
        this(id, name, internalCode, barcode, price, costPrice, description, active, stockQuantity != null ? stockQuantity.intValue() : 0, warehouse, location);
    }

    public static ProductResponse from(Product p, BigDecimal price, BigDecimal costPrice, boolean active) {
        return new ProductResponse(
            p.getId(),
            p.getName(),
            p.getInternalCode(),
            p.getBarcode(),
            price,
            costPrice,
            p.getDescription(),
            active,
            0,
            null,
            null
        );
    }
}
