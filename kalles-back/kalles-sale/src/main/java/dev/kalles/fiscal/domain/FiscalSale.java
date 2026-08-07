package dev.kalles.fiscal.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FiscalSale(
        UUID id,
        UUID tenantId,
        UUID companyId,
        FiscalSaleStatus status,
        BigDecimal total,
        List<FiscalSaleItem> items
) {
    public boolean canIssueFiscalDocument() {
        return status == FiscalSaleStatus.PAID || status == FiscalSaleStatus.COMPLETED;
    }

    public boolean hasMinimumFiscalClassification() {
        return items != null
                && !items.isEmpty()
                && items.stream().allMatch(FiscalSaleItem::hasMinimumFiscalClassification);
    }
}
