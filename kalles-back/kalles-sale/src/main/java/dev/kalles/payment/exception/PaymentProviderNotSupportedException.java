package dev.kalles.payment.exception;

import dev.kalles.payment.domain.PaymentProvider;

public class PaymentProviderNotSupportedException extends RuntimeException {

    public PaymentProviderNotSupportedException(PaymentProvider provider, String portType) {
        super("No " + portType + " registered for provider: " + provider);
    }
}
