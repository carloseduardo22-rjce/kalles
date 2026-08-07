package dev.kalles.fiscal.application.port.in;

import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.domain.FiscalTaxRegime;

import java.time.Instant;
import java.util.UUID;

public record SaveFiscalPreparationCommand(
        UUID tenantId,
        UUID companyId,
        String cnpj,
        String legalName,
        String tradeName,
        String stateRegistration,
        FiscalTaxRegime taxRegime,
        String cnae,
        String zipCode,
        String stateCode,
        Integer stateIbgeCode,
        String cityName,
        Integer cityIbgeCode,
        String district,
        String street,
        String number,
        String complement,
        String countryName,
        Integer countryCode,
        FiscalDocumentModel model,
        FiscalEnvironment environment,
        String cscId,
        String cscToken,
        Integer series,
        Long nextNumber,
        String certificateBase64,
        String certificatePassword,
        Instant certificateExpiresAt
) {
}
