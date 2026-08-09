package dev.kalles.fiscal.application.port.out;

import dev.kalles.fiscal.domain.FiscalIssuerProfile;

import java.util.Optional;
import java.util.UUID;

public interface FiscalIssuerProfileRepository {
    Optional<FiscalIssuerProfile> findByCompany(UUID tenantId, UUID companyId);

    FiscalIssuerProfile save(FiscalIssuerProfile profile);
}
