package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StoreCreateRequest(
        String name,
        @JsonProperty("external_id") String externalId,
        Location location
) {

    public record Location(
            @JsonProperty("street_name") String streetName,
            @JsonProperty("street_number") String streetNumber,
            @JsonProperty("city_name") String cityName,
            @JsonProperty("state_name") String stateName,
            Double latitude,
            Double longitude
    ) {
    }
}
