package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class ClosingAuthorizationRequiredException extends BusinessRuleViolationException {

    public ClosingAuthorizationRequiredException() {
        super("CLOSING_AUTHORIZATION_REQUIRED", "Esta contagem precisa da liberação de um supervisor para fechar o caixa.");
    }
}
