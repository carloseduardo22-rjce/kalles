package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class CashOnlySessionException extends BusinessRuleViolationException {

    public CashOnlySessionException() {
        super("CASH_ONLY_SESSION", "Esta sessao foi aberta em modo somente dinheiro. PIX, vouchers e cartoes estao indisponiveis.");
    }
}
