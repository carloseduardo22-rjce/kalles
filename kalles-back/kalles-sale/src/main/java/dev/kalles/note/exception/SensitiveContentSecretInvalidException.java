package dev.kalles.note.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class SensitiveContentSecretInvalidException extends BusinessRuleViolationException {

    public SensitiveContentSecretInvalidException() {
        super("SENSITIVE_CONTENT_SECRET_INVALID", "Segredo inválido para este conteúdo.");
    }
}
