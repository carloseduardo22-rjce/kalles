package dev.kalles.fidelity.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class LegalEntityNotEligibleForFidelityException extends BusinessRuleViolationException {

    public LegalEntityNotEligibleForFidelityException() {
        super("LEGAL_ENTITY_NOT_ELIGIBLE_FOR_FIDELITY", "Cliente pessoa jurídica não participa do programa de fidelidade.");
    }
}
