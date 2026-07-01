package dev.kalles.sale.fiscal.application.port.in;

import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;

import java.util.UUID;

public record IssueNfceCommand(
        UUID tenantId,
        UUID companyId,
        UUID saleId,
        FiscalDocumentModel model,
        FiscalEnvironment environment
) {
}
