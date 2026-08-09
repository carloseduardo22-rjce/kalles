package dev.kalles.fiscal.adapter.in.web.dto;

import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.domain.FiscalTaxRegime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SaveFiscalPreparationRequest(
        @NotBlank String cnpj,
        @NotBlank String legalName,
        String tradeName,
        @NotBlank String stateRegistration,
        @NotNull FiscalTaxRegime taxRegime,
        String cnae,
        @NotBlank String zipCode,
        @NotBlank String stateCode,
        @NotNull Integer stateIbgeCode,
        @NotBlank String cityName,
        @NotNull Integer cityIbgeCode,
        @NotBlank String district,
        @NotBlank String street,
        @NotBlank String number,
        String complement,
        String countryName,
        Integer countryCode,
        @NotNull FiscalDocumentModel model,
        @NotNull FiscalEnvironment environment,
        String cscId,
        String cscToken,
        @NotNull Integer series,
        @NotNull Long nextNumber,
        @NotBlank String certificateBase64,
        @NotBlank String certificatePassword,
        @NotNull Instant certificateExpiresAt
) {
}
