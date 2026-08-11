package dev.kalles.fiscal.adapter.out.persistence.repository;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalConfigurationEntity;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalConfigurationRepository extends JpaRepository<FiscalConfigurationEntity, UUID> {
    Optional<FiscalConfigurationEntity> findByTenantIdAndCompanyIdAndModelAndEnvironment(
            UUID tenantId,
            UUID companyId,
            FiscalDocumentModel model,
            FiscalEnvironment environment
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM FiscalConfigurationEntity c
        WHERE c.tenantId = :tenantId
          AND c.companyId = :companyId
          AND c.model = :model
          AND c.environment = :environment
    """)
    Optional<FiscalConfigurationEntity> findForNumberReservation(
            @Param("tenantId") UUID tenantId,
            @Param("companyId") UUID companyId,
            @Param("model") FiscalDocumentModel model,
            @Param("environment") FiscalEnvironment environment
    );
}
