package dev.kalles.sale.fiscal.adapter.in.web.dto;

import dev.kalles.sale.fiscal.domain.FiscalIssuerProfile;
import dev.kalles.sale.fiscal.domain.FiscalTaxRegime;

import java.util.UUID;

public record FiscalIssuerProfileResponse(
        UUID id,
        UUID tenantId,
        UUID companyId,
        String cnpj,
        String legalName,
        String tradeName,
        String stateRegistration,
        FiscalTaxRegime taxRegime,
        String cnae
) {
    public static FiscalIssuerProfileResponse from(FiscalIssuerProfile profile) {
        return new FiscalIssuerProfileResponse(profile.id(), profile.tenantId(), profile.companyId(),
                profile.cnpj(), profile.legalName(), profile.tradeName(), profile.stateRegistration(),
                profile.taxRegime(), profile.cnae());
    }
}
