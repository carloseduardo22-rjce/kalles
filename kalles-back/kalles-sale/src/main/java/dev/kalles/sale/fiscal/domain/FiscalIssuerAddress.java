package dev.kalles.sale.fiscal.domain;

import java.util.UUID;

public record FiscalIssuerAddress(
        UUID id,
        UUID tenantId,
        UUID companyId,
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
        Integer countryCode
) {
    public boolean isComplete() {
        return hasText(zipCode)
                && hasText(stateCode)
                && stateIbgeCode != null
                && hasText(cityName)
                && cityIbgeCode != null
                && hasText(district)
                && hasText(street)
                && hasText(number)
                && hasText(countryName)
                && countryCode != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
