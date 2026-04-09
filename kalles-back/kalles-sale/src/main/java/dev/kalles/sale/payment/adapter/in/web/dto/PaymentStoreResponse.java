package dev.kalles.sale.payment.adapter.in.web.dto;

import dev.kalles.sale.payment.domain.PaymentStoreView;

import java.util.Map;

public record PaymentStoreResponse(
        String providerStoreId,
        String name,
        String externalReference,
        String createdAt,
        Map<String, Object> metadata
) {

    public static PaymentStoreResponse from(PaymentStoreView store) {
        return new PaymentStoreResponse(
                store.providerStoreId(),
                store.name(),
                store.externalReference(),
                store.createdAt(),
                store.metadata()
        );
    }
}
