package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PointOrderRequest(
        String type,
        @JsonProperty("external_reference") String externalReference,
        String description,
        Config config,
        OrderTransactionsRequest transactions
) {

    public record Config(
            Point point,
            @JsonProperty("payment_method") PaymentMethod paymentMethod
    ) {
    }

    public record Point(
            @JsonProperty("terminal_id") String terminalId,
            @JsonProperty("print_on_terminal") String printOnTerminal
    ) {
    }

    public record PaymentMethod(@JsonProperty("default_type") String defaultType) {
    }
}
