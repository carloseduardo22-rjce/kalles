package dev.kalles.payment.domain;

import java.util.Map;

public record PaymentPointView(
        String providerPointId,
        String name,
        String providerStoreId,
        String externalReference,
        String externalStoreReference,
        Boolean fixedAmount,
        String status,
        String createdAt,
        String updatedAt,
        Map<String, Object> metadata
) {

    public PaymentPointView {
        metadata = DomainMetadata.immutableCopy(metadata);
    }
}
