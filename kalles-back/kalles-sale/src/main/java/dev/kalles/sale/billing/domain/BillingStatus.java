package dev.kalles.sale.billing.domain;

public enum BillingStatus {
    CHECKOUT_CREATED,
    INCOMPLETE,
    INCOMPLETE_EXPIRED,
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    UNPAID
}
