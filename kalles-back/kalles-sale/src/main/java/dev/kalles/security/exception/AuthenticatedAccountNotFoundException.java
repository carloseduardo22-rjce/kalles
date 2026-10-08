package dev.kalles.security.exception;

import dev.kalles.shared.exception.AuthenticationFailedException;

public class AuthenticatedAccountNotFoundException extends AuthenticationFailedException {

    public AuthenticatedAccountNotFoundException() {
        super("AUTHENTICATED_ACCOUNT_NOT_FOUND", "Conta autenticada nao encontrada.");
    }
}
