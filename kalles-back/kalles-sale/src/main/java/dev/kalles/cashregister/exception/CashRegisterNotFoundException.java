package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.NotFoundException;

public class CashRegisterNotFoundException extends NotFoundException {

    public CashRegisterNotFoundException(String code) {
        super("Caixa não encontrado: " + code);
    }
}
