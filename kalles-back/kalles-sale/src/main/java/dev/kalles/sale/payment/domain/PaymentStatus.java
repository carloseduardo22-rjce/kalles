package dev.kalles.sale.payment.domain;

public enum PaymentStatus {
    CREATED,
    PENDING,
    ACTION_REQUIRED,
    IN_PROGRESS,
    APPROVED,
    CANCELED,
    REFUNDED,
    EXPIRED,
    FAILED,
    UNKNOWN;

    public boolean isFinalState() {
        return this == APPROVED
                || this == CANCELED
                || this == REFUNDED
                || this == EXPIRED
                || this == FAILED;
    }
}
