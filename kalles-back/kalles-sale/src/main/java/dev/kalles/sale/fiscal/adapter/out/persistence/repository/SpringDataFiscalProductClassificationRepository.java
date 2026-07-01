package dev.kalles.sale.fiscal.adapter.out.persistence.repository;

import dev.kalles.sale.fiscal.adapter.out.persistence.entity.FiscalProductClassificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalProductClassificationRepository extends JpaRepository<FiscalProductClassificationEntity, UUID> {
    Optional<FiscalProductClassificationEntity> findByTenantIdAndCompanyIdAndProductId(UUID tenantId, UUID companyId, UUID productId);
}
