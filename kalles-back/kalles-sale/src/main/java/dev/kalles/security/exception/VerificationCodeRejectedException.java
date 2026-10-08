package dev.kalles.security.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class VerificationCodeRejectedException extends BusinessRuleViolationException {

    private VerificationCodeRejectedException(String code, String message) {
        super(code, message);
    }

    public static VerificationCodeRejectedException invalid() {
        return new VerificationCodeRejectedException("VERIFICATION_CODE_INVALID", "Código inválido ou não encontrado.");
    }

    public static VerificationCodeRejectedException alreadyUsed() {
        return new VerificationCodeRejectedException("VERIFICATION_CODE_ALREADY_USED", "Este código já foi utilizado.");
    }

    public static VerificationCodeRejectedException expired() {
        return new VerificationCodeRejectedException(
                "VERIFICATION_CODE_EXPIRED", "Este código expirou. Por favor, solicite um novo.");
    }
}
