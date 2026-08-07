package dev.kalles.core.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int stockQuantity) {
        super("Estoque insuficiente para o produto '" + productName
                + "'. Quantidade disponível: " + stockQuantity);
    }
}
