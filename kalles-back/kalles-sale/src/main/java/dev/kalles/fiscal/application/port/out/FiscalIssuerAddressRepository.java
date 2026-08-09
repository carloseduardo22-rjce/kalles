package dev.kalles.fiscal.application.port.out;

import dev.kalles.fiscal.domain.FiscalIssuerAddress;

import java.util.Optional;
import java.util.UUID;

public interface FiscalIssuerAddressRepository {
    Optional<FiscalIssuerAddress> findByCompany(UUID tenantId, UUID companyId);

    FiscalIssuerAddress save(FiscalIssuerAddress address);
}
