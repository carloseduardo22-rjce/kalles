package dev.kalles.fiscal.adapter.in.web.dto;

import dev.kalles.fiscal.domain.FiscalTaxRegime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveFiscalIssuerProfileRequest(
        @NotBlank String cnpj,
        @NotBlank String legalName,
        String tradeName,
        @NotBlank String stateRegistration,
        @NotNull FiscalTaxRegime taxRegime,
        String cnae
) {
}
