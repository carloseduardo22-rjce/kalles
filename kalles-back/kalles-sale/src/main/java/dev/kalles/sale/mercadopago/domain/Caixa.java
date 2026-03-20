package dev.kalles.sale.mercadopago.domain;

import java.util.UUID;

public record Caixa(
        UUID id,
        String externalId,
        String name,
        String companyId,
        Long mpPosId
) {
    public boolean hasPosRegistered() {
        return mpPosId != null;
    }

    public Caixa withPosId(Long posId) {
        return new Caixa(id, externalId, name, companyId, posId);
    }
}
