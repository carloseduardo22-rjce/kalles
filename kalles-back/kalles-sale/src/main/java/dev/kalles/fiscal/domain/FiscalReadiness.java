package dev.kalles.fiscal.domain;

import java.util.List;
import java.util.UUID;

public record FiscalReadiness(
        UUID tenantId,
        UUID companyId,
        boolean ready,
        List<String> missingItems
) {
}
