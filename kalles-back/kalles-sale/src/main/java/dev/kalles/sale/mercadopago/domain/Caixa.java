package dev.kalles.sale.mercadopago.domain;

public record Caixa(
        String id,
        String name,
        String companyId,
        Long mpPosId
) {
    public boolean hasPosRegistered() {
        return mpPosId != null;
    }

    public Caixa withPosId(Long posId) {
        return new Caixa(id, name, companyId, posId);
    }
}
