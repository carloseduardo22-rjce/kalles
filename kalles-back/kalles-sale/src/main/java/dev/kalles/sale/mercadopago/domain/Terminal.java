package dev.kalles.sale.mercadopago.domain;

public record Terminal(
        String id,
        String posId,
        String storeId,
        String externalPosId,
        String operationMode) {

}