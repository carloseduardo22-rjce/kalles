package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PointCreateRequest(
        String name,
        @JsonProperty("fixed_amount") Boolean fixedAmount,
        @JsonProperty("store_id") Long storeId,
        @JsonProperty("external_store_id") String externalStoreId,
        @JsonProperty("external_id") String externalId
) {
}
