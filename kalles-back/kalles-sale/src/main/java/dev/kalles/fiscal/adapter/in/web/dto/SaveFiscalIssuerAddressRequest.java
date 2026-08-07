package dev.kalles.fiscal.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveFiscalIssuerAddressRequest(
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
        Integer countryCode
) {
}
