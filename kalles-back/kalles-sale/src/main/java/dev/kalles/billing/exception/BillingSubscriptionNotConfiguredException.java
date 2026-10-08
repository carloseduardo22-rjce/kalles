package dev.kalles.billing.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class BillingSubscriptionNotConfiguredException extends BusinessRuleViolationException {

    public BillingSubscriptionNotConfiguredException() {
        super("BILLING_SUBSCRIPTION_NOT_CONFIGURED", "Nenhuma assinatura Stripe encontrada para este tenant.");
    }
}
