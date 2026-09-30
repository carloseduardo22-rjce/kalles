package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ConflictException;

public class OperatorCodeAlreadyExistsException extends ConflictException {

    public OperatorCodeAlreadyExistsException() {
        super("OPERATOR_CODE_ALREADY_EXISTS", "Já existe um operador com o código informado nesta filial.");
    }
}
