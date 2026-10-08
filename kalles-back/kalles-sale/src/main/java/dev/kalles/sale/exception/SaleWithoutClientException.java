package dev.kalles.sale.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class SaleWithoutClientException extends BusinessRuleViolationException {

    public SaleWithoutClientException() {
        super("SALE_WITHOUT_CLIENT", "Nenhum cliente associado à venda.");
    }
}
