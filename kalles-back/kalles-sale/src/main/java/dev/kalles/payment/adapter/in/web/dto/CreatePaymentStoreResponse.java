package dev.kalles.payment.adapter.in.web.dto;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;

public record CreatePaymentStoreResponse(
        PaymentProvider provider,
        String providerStoreId
) {

    public static CreatePaymentStoreResponse from(PaymentStore store) {
        return new CreatePaymentStoreResponse(store.provider(), store.providerStoreId());
    }
}
