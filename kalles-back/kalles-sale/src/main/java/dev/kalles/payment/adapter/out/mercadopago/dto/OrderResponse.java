package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OrderResponse(
        String id,
        String status,
        @JsonProperty("type_response") TypeResponse typeResponse,
        Transactions transactions
) {

    public record TypeResponse(@JsonProperty("qr_data") String qrData) {
    }

    public record Transactions(List<Payment> payments) {
    }

    public record Payment(String id) {
    }

    public String firstPaymentId() {
        if (transactions == null || transactions.payments() == null || transactions.payments().isEmpty()) {
            return null;
        }
        return transactions.payments().getFirst().id();
    }
}
