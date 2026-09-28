package dev.kalles.sale.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class EmptySaleException extends BusinessRuleViolationException {

    public EmptySaleException() {
        super("SALE_WITHOUT_ITEMS", "Não é possível iniciar pagamento sem itens na venda.");
    }
}
