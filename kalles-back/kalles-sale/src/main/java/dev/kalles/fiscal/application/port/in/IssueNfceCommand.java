package dev.kalles.fiscal.application.port.in;

import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;

import java.util.UUID;

public record IssueNfceCommand(
        UUID tenantId,
        UUID companyId,
        UUID saleId,
        FiscalDocumentModel model,
        FiscalEnvironment environment
) {
}
