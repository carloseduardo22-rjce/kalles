package dev.kalles.fiscal.application.port.out;

import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;

import java.util.Optional;
import java.util.UUID;

public interface FiscalConfigurationRepository {
    Optional<FiscalConfiguration> findByCompany(UUID tenantId, UUID companyId, FiscalDocumentModel model, FiscalEnvironment environment);

    long reserveNextNumber(UUID tenantId, UUID companyId, FiscalDocumentModel model, FiscalEnvironment environment);

    FiscalConfiguration save(FiscalConfiguration configuration);
}
