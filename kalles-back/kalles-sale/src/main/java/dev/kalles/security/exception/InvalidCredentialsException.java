package dev.kalles.security.exception;

import dev.kalles.shared.exception.AuthenticationFailedException;

public class InvalidCredentialsException extends AuthenticationFailedException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Credenciais invalidas.");
    }
}
