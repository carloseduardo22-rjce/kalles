package dev.kalles.sale.mercadopago.domain;

public enum PointTransactionStatus {
    CREATED,
    PROCESSED,
    ACTION_REQUIRED,
    AT_TERMINAL,
    EXPIRED,
    REFUNDED,
    CANCELED,
    FAILED
}
