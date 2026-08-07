package dev.kalles.payment.domain;

import java.util.Objects;
import java.util.UUID;

public record PaymentPoint(
        UUID id,
        UUID cashRegisterId,
        PaymentProvider provider,
        String externalReference,
        String providerPointId
) {

    public PaymentPoint {
        Objects.requireNonNull(cashRegisterId, "cashRegisterId is required");
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(externalReference, "externalReference is required");
    }

    public boolean hasProviderPoint() {
        return providerPointId != null && !providerPointId.isBlank();
    }

    public PaymentPoint withProviderPointId(String newProviderPointId) {
        return new PaymentPoint(id, cashRegisterId, provider, externalReference, newProviderPointId);
    }
}
