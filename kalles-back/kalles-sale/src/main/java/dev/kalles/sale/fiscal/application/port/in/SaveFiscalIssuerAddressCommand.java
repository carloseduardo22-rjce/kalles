package dev.kalles.sale.fiscal.application.port.in;

import java.util.UUID;

public record SaveFiscalIssuerAddressCommand(
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
}
