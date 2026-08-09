package dev.kalles.fiscal.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record FiscalSaleItem(
        UUID productId,
        String description,
        String ncm,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal total
) {
    public boolean hasMinimumFiscalClassification() {
        return ncm != null && !ncm.isBlank();
    }
}
