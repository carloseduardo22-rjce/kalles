package dev.kalles.sale.fiscal.adapter.out.persistence.repository;

import dev.kalles.sale.fiscal.adapter.out.persistence.entity.FiscalConfigurationEntity;
import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalConfigurationRepository extends JpaRepository<FiscalConfigurationEntity, UUID> {
    Optional<FiscalConfigurationEntity> findByTenantIdAndCompanyIdAndModelAndEnvironment(
            UUID tenantId,
            UUID companyId,
            FiscalDocumentModel model,
            FiscalEnvironment environment
    );
}
