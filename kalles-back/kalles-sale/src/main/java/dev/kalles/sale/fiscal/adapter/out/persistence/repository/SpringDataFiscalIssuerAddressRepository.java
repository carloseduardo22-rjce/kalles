package dev.kalles.sale.fiscal.adapter.out.persistence.repository;

import dev.kalles.sale.fiscal.adapter.out.persistence.entity.FiscalIssuerAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalIssuerAddressRepository extends JpaRepository<FiscalIssuerAddressEntity, UUID> {
    Optional<FiscalIssuerAddressEntity> findByTenantIdAndCompanyId(UUID tenantId, UUID companyId);
}
