package dev.kalles.fiscal.adapter.in.web.dto;

import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;

import java.time.Instant;
import java.util.UUID;

public record FiscalDocumentResponse(
        UUID id,
        UUID tenantId,
        UUID companyId,
        UUID saleId,
        FiscalDocumentModel model,
        FiscalEnvironment environment,
        FiscalDocumentStatus status,
        String accessKey,
        String authorizationProtocol,
        String rejectionReason,
        Instant issuedAt
) {
    public static FiscalDocumentResponse from(FiscalDocument document) {
        return new FiscalDocumentResponse(
                document.id(),
                document.tenantId(),
                document.companyId(),
                document.saleId(),
                document.model(),
                document.environment(),
                document.status(),
                document.accessKey(),
                document.authorizationProtocol(),
                document.rejectionReason(),
                document.issuedAt()
        );
    }
}
