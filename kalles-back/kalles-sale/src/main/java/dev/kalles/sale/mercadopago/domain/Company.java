package dev.kalles.sale.mercadopago.domain;

import java.util.UUID;

public record Company(
        UUID id,
        String name,
        String streetName,
        String streetNumber,
        String cityName,
        String stateName,
        double latitude,
        double longitude,
        Long mpStoreId) {
    public boolean hasStoreRegistered() {
        return mpStoreId != null;
    }

    public Company withStoreId(Long storeId) {
        return new Company(id, name, streetName, streetNumber, cityName, stateName, latitude, longitude, storeId);
    }
}
