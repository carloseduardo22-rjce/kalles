package dev.kalles.security.exception;

import dev.kalles.shared.exception.AuthenticationFailedException;

public class InvalidPosSessionException extends AuthenticationFailedException {

    public InvalidPosSessionException() {
        super("POS_SESSION_INVALID", "Sessão do terminal inválida ou expirada.");
    }
}
