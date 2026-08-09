package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentProvider;

public record PaymentStoreStatusResponse(
        PaymentProvider provider,
        boolean companyExists,
        boolean hasStoreRegistered,
        String externalReference,
        String providerStoreId
) {
}
