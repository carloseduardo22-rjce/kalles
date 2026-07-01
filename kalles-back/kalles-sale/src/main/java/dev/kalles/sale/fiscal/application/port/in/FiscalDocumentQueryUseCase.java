package dev.kalles.sale.fiscal.application.port.in;

import dev.kalles.sale.fiscal.domain.FiscalDocument;

import java.util.UUID;

public interface FiscalDocumentQueryUseCase {
    FiscalDocument getStatus(UUID tenantId, UUID companyId, UUID documentId);

    byte[] renderDanfe(UUID tenantId, UUID companyId, UUID documentId);
}
