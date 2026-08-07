package dev.kalles.fiscal.application.port.out;

import dev.kalles.fiscal.domain.FiscalProductClassification;

import java.util.Optional;
import java.util.UUID;

public interface FiscalProductClassificationRepository {
    Optional<FiscalProductClassification> findByProduct(UUID tenantId, UUID companyId, UUID productId);

    FiscalProductClassification save(FiscalProductClassification classification);
}
