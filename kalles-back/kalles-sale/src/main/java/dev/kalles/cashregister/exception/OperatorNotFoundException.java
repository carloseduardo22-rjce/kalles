package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.NotFoundException;

public class OperatorNotFoundException extends NotFoundException {

    public OperatorNotFoundException(String code) {
        super("Operador não encontrado: " + code);
    }
}
