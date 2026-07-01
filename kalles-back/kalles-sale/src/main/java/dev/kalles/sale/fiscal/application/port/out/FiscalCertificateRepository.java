package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalCertificate;

import java.util.Optional;
import java.util.UUID;

public interface FiscalCertificateRepository {
    Optional<FiscalCertificate> findActiveByCompany(UUID tenantId, UUID companyId);

    FiscalCertificate save(FiscalCertificate certificate);

    void deactivateActiveByCompany(UUID tenantId, UUID companyId);
}
