package dev.kalles.sale.fiscal.adapter.in.web.dto;

import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveFiscalConfigurationRequest(
        @NotNull FiscalDocumentModel model,
        @NotNull FiscalEnvironment environment,
        @NotBlank String stateCode,
        String cscId,
        String cscToken,
        @NotNull Integer series,
        @NotNull Long nextNumber
) {
}
