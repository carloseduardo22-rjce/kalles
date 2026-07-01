package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalDocument;
import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalDocumentStatus;

import java.util.Optional;
import java.util.UUID;

public interface FiscalDocumentRepository {
    boolean existsBySaleAndStatus(UUID tenantId, UUID companyId, UUID saleId, FiscalDocumentModel model, FiscalDocumentStatus status);

    FiscalDocument save(FiscalDocument document);

    Optional<FiscalDocument> findBySale(UUID tenantId, UUID companyId, UUID saleId, FiscalDocumentModel model);

    Optional<FiscalDocument> findById(UUID tenantId, UUID companyId, UUID documentId);

    Optional<FiscalDocument> findAuthorizedBySale(UUID tenantId, UUID companyId, UUID saleId, FiscalDocumentModel model);
}
