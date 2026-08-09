package dev.kalles.fiscal.application.port.in;

import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;

import java.util.UUID;

public record SaveFiscalConfigurationCommand(
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
}
