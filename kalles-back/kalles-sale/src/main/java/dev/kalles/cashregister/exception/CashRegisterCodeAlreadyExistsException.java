package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ConflictException;

public class CashRegisterCodeAlreadyExistsException extends ConflictException {

    public CashRegisterCodeAlreadyExistsException(String code) {
        super("CASH_REGISTER_CODE_ALREADY_EXISTS", "Já existe um caixa com este código nesta filial: " + code);
    }
}
