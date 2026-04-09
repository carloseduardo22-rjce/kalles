package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;

public record CreatePaymentStoreResponse(
        PaymentProvider provider,
        String providerStoreId
) {

    public static CreatePaymentStoreResponse from(PaymentStore store) {
        return new CreatePaymentStoreResponse(store.provider(), store.providerStoreId());
    }
}
