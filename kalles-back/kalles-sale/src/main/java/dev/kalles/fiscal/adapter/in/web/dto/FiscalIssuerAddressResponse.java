package dev.kalles.fiscal.adapter.in.web.dto;

import dev.kalles.fiscal.domain.FiscalIssuerAddress;

import java.util.UUID;

public record FiscalIssuerAddressResponse(
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
    public static FiscalIssuerAddressResponse from(FiscalIssuerAddress address) {
        return new FiscalIssuerAddressResponse(address.id(), address.tenantId(), address.companyId(),
                address.zipCode(), address.stateCode(), address.stateIbgeCode(), address.cityName(),
                address.cityIbgeCode(), address.district(), address.street(), address.number(),
                address.complement(), address.countryName(), address.countryCode());
    }
}
