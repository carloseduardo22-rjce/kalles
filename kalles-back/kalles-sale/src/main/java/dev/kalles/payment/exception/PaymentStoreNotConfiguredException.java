package dev.kalles.payment.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentStoreNotConfiguredException extends BusinessRuleViolationException {

    public PaymentStoreNotConfiguredException() {
        super("PAYMENT_STORE_NOT_CONFIGURED", "A filial nao tem loja configurada no provedor de pagamento.");
    }
}
