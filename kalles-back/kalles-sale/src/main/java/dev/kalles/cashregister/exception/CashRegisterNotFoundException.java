package dev.kalles.cashregister.exception;

public class CashRegisterNotFoundException extends RuntimeException {
    public CashRegisterNotFoundException(String code) {
        super("Caixa não encontrado: " + code);
    }
}
