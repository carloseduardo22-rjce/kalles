package dev.kalles.sale.fiscal.adapter.out.persistence.repository;

import dev.kalles.sale.fiscal.adapter.out.persistence.entity.FiscalCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalCertificateRepository extends JpaRepository<FiscalCertificateEntity, UUID> {
    Optional<FiscalCertificateEntity> findFirstByTenantIdAndCompanyIdAndActiveTrueOrderByExpiresAtDesc(UUID tenantId, UUID companyId);

    java.util.List<FiscalCertificateEntity> findAllByTenantIdAndCompanyIdAndActiveTrue(UUID tenantId, UUID companyId);
}
