package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentProvider;

public record PaymentStoreStatusResponse(
        PaymentProvider provider,
        boolean companyExists,
        boolean hasStoreRegistered,
        String externalReference,
        String providerStoreId
) {
}
