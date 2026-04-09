package dev.kalles.sale.payment.domain;

import java.util.Map;

public record PaymentStoreView(
        String providerStoreId,
        String name,
        String externalReference,
        String createdAt,
        Map<String, Object> metadata
) {

    public PaymentStoreView {
        metadata = DomainMetadata.immutableCopy(metadata);
    }
}
