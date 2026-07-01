package dev.kalles.sale.fiscal.domain;

import java.util.UUID;

public record FiscalIssuerProfile(
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
    public boolean hasRequiredNfceIdentification() {
        return hasText(cnpj)
                && hasText(legalName)
                && hasText(stateRegistration)
                && taxRegime != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
