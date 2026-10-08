package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ConflictException;

public class OperatorAlreadyInSessionException extends ConflictException {

    public OperatorAlreadyInSessionException(String operatorCode) {
        super("OPERATOR_ALREADY_IN_SESSION", "O operador " + operatorCode + " já está vinculado a uma sessão ativa em outro caixa");
    }
}
