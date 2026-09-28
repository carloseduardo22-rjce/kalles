package dev.kalles.sale.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentExceedsBalanceException extends BusinessRuleViolationException {

    public PaymentExceedsBalanceException() {
        super("PAYMENT_EXCEEDS_BALANCE", "O valor do pagamento excede o saldo devedor da venda.");
    }
}
