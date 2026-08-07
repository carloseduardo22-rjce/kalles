package dev.kalles.fiscal.adapter.out.persistence.repository;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalIssuerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalIssuerProfileRepository extends JpaRepository<FiscalIssuerProfileEntity, UUID> {
    Optional<FiscalIssuerProfileEntity> findByTenantIdAndCompanyId(UUID tenantId, UUID companyId);
}
