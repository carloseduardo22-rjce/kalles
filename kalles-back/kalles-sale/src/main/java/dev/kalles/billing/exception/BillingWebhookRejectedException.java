package dev.kalles.billing.exception;

public class BillingWebhookRejectedException extends RuntimeException {

    public static final String CODE = "BILLING_WEBHOOK_REJECTED";

    public BillingWebhookRejectedException(Throwable cause) {
        super("Assinatura do webhook Stripe invalida.", cause);
    }
}
