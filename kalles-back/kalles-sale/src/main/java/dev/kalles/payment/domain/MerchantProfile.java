package dev.kalles.payment.domain;

public record MerchantProfile(
        String name,
        String streetName,
        String streetNumber,
        String cityName,
        String stateName,
        Double latitude,
        Double longitude
) {
}
