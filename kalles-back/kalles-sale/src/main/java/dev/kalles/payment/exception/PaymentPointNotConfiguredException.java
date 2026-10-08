package dev.kalles.payment.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentPointNotConfiguredException extends BusinessRuleViolationException {

    public PaymentPointNotConfiguredException() {
        super("PAYMENT_POINT_NOT_CONFIGURED", "O caixa nao tem ponto de venda configurado no provedor de pagamento.");
    }
}
