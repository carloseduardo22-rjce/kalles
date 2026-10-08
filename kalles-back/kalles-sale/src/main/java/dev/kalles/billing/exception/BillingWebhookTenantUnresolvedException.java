package dev.kalles.billing.exception;

import dev.kalles.shared.exception.ConflictException;

public class BillingWebhookTenantUnresolvedException extends ConflictException {

    public BillingWebhookTenantUnresolvedException() {
        super("BILLING_WEBHOOK_TENANT_UNRESOLVED", "Nao foi possivel resolver o tenant da notificacao Stripe.");
    }
}
