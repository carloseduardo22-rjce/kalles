package dev.kalles.fiscal.adapter.out.persistence.repository;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalDocumentEntity;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataFiscalDocumentRepository extends JpaRepository<FiscalDocumentEntity, UUID> {
    boolean existsByTenantIdAndCompanyIdAndSaleIdAndModelAndStatus(
            UUID tenantId,
            UUID companyId,
            UUID saleId,
            FiscalDocumentModel model,
            FiscalDocumentStatus status
    );

    Optional<FiscalDocumentEntity> findFirstByTenantIdAndCompanyIdAndSaleIdAndModelOrderByIssuedAtDesc(
            UUID tenantId,
            UUID companyId,
            UUID saleId,
            FiscalDocumentModel model
    );

    Optional<FiscalDocumentEntity> findByIdAndTenantIdAndCompanyId(UUID id, UUID tenantId, UUID companyId);

    Optional<FiscalDocumentEntity> findFirstByTenantIdAndCompanyIdAndSaleIdAndModelAndStatusOrderByIssuedAtDesc(
            UUID tenantId,
            UUID companyId,
            UUID saleId,
            FiscalDocumentModel model,
            FiscalDocumentStatus status
    );
}
