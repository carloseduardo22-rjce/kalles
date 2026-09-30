package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PaymentIntegrationNotConfiguredException extends BusinessRuleViolationException {

    public PaymentIntegrationNotConfiguredException() {
        super("PAYMENT_INTEGRATION_NOT_CONFIGURED",
                "Pagamento nao configurado, neste caixa voce apenas podera operar com dinheiro mas nao podera receber pagamentos via pix, vouchers e cartoes de credito.");
    }
}
