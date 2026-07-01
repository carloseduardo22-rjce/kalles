package dev.kalles.sale.fiscal.adapter.in.web.dto;

import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;

import java.util.UUID;

public record FiscalConfigurationResponse(
        UUID id,
        UUID tenantId,
        UUID companyId,
        FiscalDocumentModel model,
        FiscalEnvironment environment,
        String stateCode,
        String cscId,
        Integer series,
        Long nextNumber
) {
    public static FiscalConfigurationResponse from(FiscalConfiguration configuration) {
        return new FiscalConfigurationResponse(configuration.id(), configuration.tenantId(), configuration.companyId(),
                configuration.model(), configuration.environment(), configuration.stateCode(), configuration.cscId(),
                configuration.series(), configuration.nextNumber());
    }
}
