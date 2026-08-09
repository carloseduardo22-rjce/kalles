package dev.kalles.billing.exception;

public class BillingIntegrationException extends RuntimeException {

    public BillingIntegrationException(String message) {
        super(message);
    }

    public BillingIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
