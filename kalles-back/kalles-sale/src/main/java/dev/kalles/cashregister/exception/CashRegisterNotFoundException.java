package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.NotFoundException;

public class CashRegisterNotFoundException extends NotFoundException {

    public CashRegisterNotFoundException(String code) {
        super("CASH_REGISTER_NOT_FOUND", "Caixa não encontrado: " + code);
    }
}
