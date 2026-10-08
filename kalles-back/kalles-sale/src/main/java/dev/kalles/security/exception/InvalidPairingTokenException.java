package dev.kalles.security.exception;

import dev.kalles.shared.exception.AuthenticationFailedException;

public class InvalidPairingTokenException extends AuthenticationFailedException {

    public InvalidPairingTokenException() {
        super("PAIRING_TOKEN_INVALID", "Token de pareamento inválido ou expirado.");
    }
}
