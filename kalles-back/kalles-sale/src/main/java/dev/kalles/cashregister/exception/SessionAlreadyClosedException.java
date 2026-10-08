package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ConflictException;

public class SessionAlreadyClosedException extends ConflictException {

    public SessionAlreadyClosedException() {
        super("CASH_REGISTER_SESSION_ALREADY_CLOSED", "Sessao ja esta fechada");
    }
}
