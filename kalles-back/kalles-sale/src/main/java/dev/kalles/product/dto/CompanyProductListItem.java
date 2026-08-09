package dev.kalles.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyProductListItem(
    UUID productId,
    String name,
    String internalCode,
    String barcode,
    BigDecimal price,
    BigDecimal costPrice,
    String description,
    boolean active
) {}
