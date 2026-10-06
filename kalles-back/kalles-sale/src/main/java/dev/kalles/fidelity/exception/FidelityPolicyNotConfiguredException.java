package dev.kalles.fidelity.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class FidelityPolicyNotConfiguredException extends BusinessRuleViolationException {

    public FidelityPolicyNotConfiguredException() {
        super("FIDELITY_POLICY_NOT_CONFIGURED", "Nenhuma política de fidelidade ativa encontrada para esta filial.");
    }
}
