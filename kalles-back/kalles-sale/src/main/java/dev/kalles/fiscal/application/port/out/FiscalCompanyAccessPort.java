package dev.kalles.fiscal.application.port.out;

import java.util.UUID;

public interface FiscalCompanyAccessPort {
    boolean existsByTenant(UUID tenantId, UUID companyId);
}
