package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.NotFoundException;

public class ClosingAuthorizerNotFoundException extends NotFoundException {

    public ClosingAuthorizerNotFoundException(String operatorCode) {
        super("CLOSING_AUTHORIZER_NOT_FOUND", "Operador autorizador nao encontrado: " + operatorCode);
    }
}
