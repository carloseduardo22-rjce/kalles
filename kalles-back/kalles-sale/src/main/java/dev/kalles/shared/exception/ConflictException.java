package dev.kalles.shared.exception;

public abstract non-sealed class ConflictException extends DomainException {

    protected ConflictException(String code, String message) {
        super(code, message);
    }

    protected ConflictException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
