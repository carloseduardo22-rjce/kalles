package dev.kalles.sale.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class SaleBalancePendingException extends BusinessRuleViolationException {

    public SaleBalancePendingException() {
        super("SALE_BALANCE_PENDING", "Não é possível finalizar a venda: ainda há valores pendentes de pagamento.");
    }
}
