package dev.kalles.sale.mercadopago.domain;

public enum PointOrderStatus {
    CREATED,
    PROCESSED,
    ACTION_REQUIRED,
    AT_TERMINAL,
    FAILED,
    REFUNDED,
    EXPIRED
}
