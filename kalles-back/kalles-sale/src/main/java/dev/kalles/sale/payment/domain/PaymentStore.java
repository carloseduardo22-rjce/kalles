package dev.kalles.sale.payment.domain;

import java.util.Objects;
import java.util.UUID;

public record PaymentStore(
        UUID id,
        UUID companyId,
        PaymentProvider provider,
        String externalReference,
        String providerStoreId
) {

    public PaymentStore {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(externalReference, "externalReference is required");
    }

    public boolean hasProviderStore() {
        return providerStoreId != null && !providerStoreId.isBlank();
    }

    public PaymentStore withProviderStoreId(String newProviderStoreId) {
        return new PaymentStore(id, companyId, provider, externalReference, newProviderStoreId);
    }
}
