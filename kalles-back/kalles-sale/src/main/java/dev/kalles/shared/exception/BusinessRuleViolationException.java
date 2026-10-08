package dev.kalles.shared.exception;

public abstract non-sealed class BusinessRuleViolationException extends DomainException {

    protected BusinessRuleViolationException(String code, String message) {
        super(code, message);
    }
}
