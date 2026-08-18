package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QrOrderRequest(
        String type,
        @JsonProperty("total_amount") String totalAmount,
        @JsonProperty("external_reference") String externalReference,
        Config config,
        OrderTransactionsRequest transactions
) {

    public record Config(Qr qr) {
    }

    public record Qr(
            @JsonProperty("external_pos_id") String externalPosId,
            String mode
    ) {
    }
}
