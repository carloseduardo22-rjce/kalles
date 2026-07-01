package dev.kalles.sale.fiscal.domain;

import java.util.UUID;

public record FiscalConfiguration(
        UUID id,
        UUID tenantId,
        UUID companyId,
        FiscalDocumentModel model,
        FiscalEnvironment environment,
        String stateCode,
        String cscId,
        String cscToken,
        Integer series,
        Long nextNumber
) {
    public boolean supports(FiscalDocumentModel requestedModel, FiscalEnvironment requestedEnvironment) {
        return model == requestedModel && environment == requestedEnvironment;
    }
}
