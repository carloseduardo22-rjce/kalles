package dev.kalles.cashregister.exception;

public class OperatorNotFoundException extends RuntimeException {
    public OperatorNotFoundException(String code) {
        super("Operador não encontrado: " + code);
    }
}
