package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PointResponse(
        String id,
        String name,
        @JsonProperty("store_id") String storeId,
        @JsonProperty("external_id") String externalId,
        @JsonProperty("external_store_id") String externalStoreId,
        @JsonProperty("fixed_amount") Boolean fixedAmount,
        String status,
        @JsonProperty("date_created") String dateCreated,
        @JsonProperty("date_last_updated") String dateLastUpdated
) {
}
