package dev.kalles.sale.mercadopago.domain;

import java.util.UUID;

public record Caixa(
        UUID id,
        String externalId,
        UUID cashRegisterId,
        Long mpPosId
) {
    public boolean hasPosRegistered() {
        return mpPosId != null;
    }

    public Caixa withPosId(Long posId) {
        return new Caixa(id, externalId, cashRegisterId, posId);
    }
}
