package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TerminalSetupRequest(List<Terminal> terminals) {

    public record Terminal(
            String id,
            @JsonProperty("operating_mode") String operatingMode
    ) {
    }
}
