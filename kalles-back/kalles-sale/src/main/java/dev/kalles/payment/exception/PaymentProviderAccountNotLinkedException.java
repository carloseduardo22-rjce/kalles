package dev.kalles.payment.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentProviderAccountNotLinkedException extends BusinessRuleViolationException {

    public PaymentProviderAccountNotLinkedException() {
        super("PAYMENT_PROVIDER_ACCOUNT_NOT_LINKED", "A conta do provedor de pagamento nao esta vinculada.");
    }
}
