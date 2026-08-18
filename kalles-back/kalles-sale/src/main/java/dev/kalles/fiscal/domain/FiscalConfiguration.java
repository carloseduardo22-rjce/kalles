package dev.kalles.fiscal.domain;

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

    public FiscalConfiguration withDocumentNumber(long documentNumber) {
        return new FiscalConfiguration(id, tenantId, companyId, model, environment,
                stateCode, cscId, cscToken, series, documentNumber);
    }
}
