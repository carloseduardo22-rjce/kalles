package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ConflictException;

public class ActiveSessionAlreadyExistsException extends ConflictException {

    public ActiveSessionAlreadyExistsException(String cashRegisterCode) {
        super("CASH_REGISTER_SESSION_ALREADY_OPEN", "O caixa " + cashRegisterCode + " já possui uma sessão ativa");
    }
}
