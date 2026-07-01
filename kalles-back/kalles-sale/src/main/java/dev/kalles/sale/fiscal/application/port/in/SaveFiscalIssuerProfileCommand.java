package dev.kalles.sale.fiscal.application.port.in;

import dev.kalles.sale.fiscal.domain.FiscalTaxRegime;

import java.util.UUID;

public record SaveFiscalIssuerProfileCommand(
        UUID tenantId,
        UUID companyId,
        String cnpj,
        String legalName,
        String tradeName,
        String stateRegistration,
        FiscalTaxRegime taxRegime,
        String cnae
) {
}
