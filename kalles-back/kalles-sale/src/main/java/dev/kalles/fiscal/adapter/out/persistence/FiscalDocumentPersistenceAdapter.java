package dev.kalles.fiscal.adapter.out.persistence;

import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalDocumentEntity;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalDocumentRepository;
import dev.kalles.fiscal.application.port.out.FiscalDocumentRepository;
import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FiscalDocumentPersistenceAdapter implements FiscalDocumentRepository {

    private final SpringDataFiscalDocumentRepository repository;

    @Override
    public boolean existsBySaleAndStatus(UUID tenantId, UUID companyId, UUID saleId, FiscalDocumentModel model, FiscalDocumentStatus status) {
        return repository.existsByTenantIdAndCompanyIdAndSaleIdAndModelAndStatus(tenantId, companyId, saleId, model, status);
    }

    @Override
    public FiscalDocument save(FiscalDocument document) {
        return repository.save(FiscalDocumentEntity.fromDomain(document)).toDomain();
    }

    @Override
    public Optional<FiscalDocument> findBySale(UUID tenantId, UUID companyId, UUID saleId, FiscalDocumentModel model) {
        return repository.findFirstByTenantIdAndCompanyIdAndSaleIdAndModelOrderByIssuedAtDesc(tenantId, companyId, saleId, model)
                .map(FiscalDocumentEntity::toDomain);
    }

    @Override
    public Optional<FiscalDocument> findById(UUID tenantId, UUID companyId, UUID documentId) {
        return repository.findByIdAndTenantIdAndCompanyId(documentId, tenantId, companyId)
                .map(FiscalDocumentEntity::toDomain);
    }

    @Override
    public Optional<FiscalDocument> findAuthorizedBySale(UUID tenantId, UUID companyId, UUID saleId, FiscalDocumentModel model) {
        return repository.findFirstByTenantIdAndCompanyIdAndSaleIdAndModelAndStatusOrderByIssuedAtDesc(
                        tenantId, companyId, saleId, model, FiscalDocumentStatus.AUTORIZADO)
                .map(FiscalDocumentEntity::toDomain);
    }
}
