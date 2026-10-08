package dev.kalles.inventory.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class InsufficientStockException extends BusinessRuleViolationException {

    public InsufficientStockException(String productName, int stockQuantity) {
        super("INSUFFICIENT_STOCK", "Estoque insuficiente para o produto '" + productName
                + "'. Quantidade disponível: " + stockQuantity);
    }
}
