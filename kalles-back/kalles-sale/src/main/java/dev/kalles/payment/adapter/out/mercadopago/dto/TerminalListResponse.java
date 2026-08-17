package dev.kalles.payment.adapter.out.mercadopago.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public record TerminalListResponse(List<Terminal> terminals) {

    public record Terminal(
            String id,
            @JsonAlias("pos_id") String posId,
            @JsonAlias("store_id") String storeId,
            @JsonAlias("external_pos_id") String externalPosId,
            @JsonAlias({"operating_mode", "operationMode"}) String operatingMode
    ) {
    }
}
