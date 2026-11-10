package dev.kalles.security.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PairingCodeInvalidException extends BusinessRuleViolationException {

    public PairingCodeInvalidException() {
        super("PAIRING_CODE_INVALID", "Código de pareamento inválido, já usado ou vencido.");
    }
}
