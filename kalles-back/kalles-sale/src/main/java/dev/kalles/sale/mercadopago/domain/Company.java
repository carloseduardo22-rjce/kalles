package dev.kalles.sale.mercadopago.domain;

import java.util.UUID;

public record Company(
        UUID id,
        UUID companyId,
        String externalId,
        Long mpStoreId) {
            
    public boolean hasStoreRegistered() {
        return mpStoreId != null;
    }

    public Company withStoreId(Long storeId) {
        return new Company(id, companyId, externalId, storeId);
    }
}
