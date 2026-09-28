package dev.kalles.sale.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentMethodUnavailableException extends BusinessRuleViolationException {

    public PaymentMethodUnavailableException(String message) {
        super("PAYMENT_METHOD_UNAVAILABLE", message);
    }
}
