package dev.kalles.shared.exception;

public abstract non-sealed class AuthenticationFailedException extends DomainException {

    protected AuthenticationFailedException(String code, String message) {
        super(code, message);
    }
}
