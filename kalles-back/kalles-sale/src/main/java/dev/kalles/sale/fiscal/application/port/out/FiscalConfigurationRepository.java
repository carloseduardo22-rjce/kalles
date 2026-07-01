package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;

import java.util.Optional;
import java.util.UUID;

public interface FiscalConfigurationRepository {
    Optional<FiscalConfiguration> findByCompany(UUID tenantId, UUID companyId, FiscalDocumentModel model, FiscalEnvironment environment);

    FiscalConfiguration save(FiscalConfiguration configuration);
}
