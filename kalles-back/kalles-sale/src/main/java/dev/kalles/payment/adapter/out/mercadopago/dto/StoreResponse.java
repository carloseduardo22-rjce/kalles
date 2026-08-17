package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StoreResponse(
        String id,
        String name,
        @JsonProperty("external_id") String externalId,
        @JsonProperty("date_creation") String dateCreation
) {
}
