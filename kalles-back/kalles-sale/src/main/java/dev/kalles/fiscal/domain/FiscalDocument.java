package dev.kalles.fiscal.domain;

import java.time.Instant;
import java.util.UUID;

public record FiscalDocument(
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
        String authorizedXml,
        Instant issuedAt
) {
    public static FiscalDocument pending(
            UUID tenantId,
            UUID companyId,
            UUID saleId,
            FiscalDocumentModel model,
            FiscalEnvironment environment,
            Instant issuedAt
    ) {
        return new FiscalDocument(null, tenantId, companyId, saleId, model, environment,
                FiscalDocumentStatus.PENDENTE, null, null, null, null, issuedAt);
    }

    public static FiscalDocument authorized(
            UUID tenantId,
            UUID companyId,
            UUID saleId,
            FiscalDocumentModel model,
            FiscalEnvironment environment,
            SefazAuthorizationResult result,
            Instant issuedAt
    ) {
        return new FiscalDocument(null, tenantId, companyId, saleId, model, environment,
                FiscalDocumentStatus.AUTORIZADO, result.accessKey(), result.authorizationProtocol(), null,
                result.authorizedXml(), issuedAt);
    }

    public FiscalDocument authorizedWith(SefazAuthorizationResult result) {
        return new FiscalDocument(id, tenantId, companyId, saleId, model, environment,
                FiscalDocumentStatus.AUTORIZADO, result.accessKey(), result.authorizationProtocol(), null,
                result.authorizedXml(), issuedAt);
    }

    public FiscalDocument rejectedWith(SefazAuthorizationResult result) {
        return new FiscalDocument(id, tenantId, companyId, saleId, model, environment,
                FiscalDocumentStatus.REJEITADO, null, null, result.rejectionReason(), null, issuedAt);
    }
}
