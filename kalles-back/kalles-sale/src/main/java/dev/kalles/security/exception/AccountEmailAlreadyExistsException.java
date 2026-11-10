package dev.kalles.security.exception;

import dev.kalles.shared.exception.ConflictException;

public class AccountEmailAlreadyExistsException extends ConflictException {

    public AccountEmailAlreadyExistsException() {
        super("ACCOUNT_EMAIL_ALREADY_EXISTS", "Já existe uma conta com o e-mail informado.");
    }
}
