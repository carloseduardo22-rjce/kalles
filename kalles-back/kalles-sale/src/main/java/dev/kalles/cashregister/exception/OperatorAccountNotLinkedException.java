package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class OperatorAccountNotLinkedException extends BusinessRuleViolationException {

    public OperatorAccountNotLinkedException() {
        super("OPERATOR_ACCOUNT_NOT_LINKED", "Esta conta não possui cadastro de operador ativo nesta filial.");
    }
}
