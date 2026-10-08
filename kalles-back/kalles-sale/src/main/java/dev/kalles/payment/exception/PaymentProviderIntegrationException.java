package dev.kalles.payment.exception;

public class PaymentProviderIntegrationException extends RuntimeException {

    public PaymentProviderIntegrationException(String message) {
        super(message);
    }

    public PaymentProviderIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
