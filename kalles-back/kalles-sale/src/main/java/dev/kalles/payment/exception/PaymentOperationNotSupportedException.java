package dev.kalles.payment.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentOperationNotSupportedException extends BusinessRuleViolationException {

    public PaymentOperationNotSupportedException(String message) {
        super("PAYMENT_OPERATION_NOT_SUPPORTED", message);
    }
}
