package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalIssuerProfile;

import java.util.Optional;
import java.util.UUID;

public interface FiscalIssuerProfileRepository {
    Optional<FiscalIssuerProfile> findByCompany(UUID tenantId, UUID companyId);

    FiscalIssuerProfile save(FiscalIssuerProfile profile);
}
