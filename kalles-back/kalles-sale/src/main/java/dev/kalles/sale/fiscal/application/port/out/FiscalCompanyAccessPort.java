package dev.kalles.sale.fiscal.application.port.out;

import java.util.UUID;

public interface FiscalCompanyAccessPort {
    boolean existsByTenant(UUID tenantId, UUID companyId);
}
