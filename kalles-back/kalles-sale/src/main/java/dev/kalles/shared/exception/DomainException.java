package dev.kalles.shared.exception;

public abstract sealed class DomainException extends RuntimeException
        permits ConflictException, BusinessRuleViolationException {

    private final String code;

    DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
