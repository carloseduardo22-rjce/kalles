package dev.kalles.sale.fiscal.adapter.in.web.dto;

import dev.kalles.sale.fiscal.domain.FiscalReadiness;

import java.util.List;
import java.util.UUID;

public record FiscalReadinessResponse(
        UUID tenantId,
        UUID companyId,
        boolean ready,
        List<String> missingItems
) {
    public static FiscalReadinessResponse from(FiscalReadiness readiness) {
        return new FiscalReadinessResponse(readiness.tenantId(), readiness.companyId(),
                readiness.ready(), readiness.missingItems());
    }
}
