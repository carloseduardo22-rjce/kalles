package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalIssuerAddress;

import java.util.Optional;
import java.util.UUID;

public interface FiscalIssuerAddressRepository {
    Optional<FiscalIssuerAddress> findByCompany(UUID tenantId, UUID companyId);

    FiscalIssuerAddress save(FiscalIssuerAddress address);
}
