package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ForbiddenOperationException;

public class ClosingAuthorizerNotAllowedException extends ForbiddenOperationException {

    public ClosingAuthorizerNotAllowedException() {
        super("CLOSING_AUTHORIZER_NOT_ALLOWED", "Operador sem permissao para liberar fechamento de caixa.");
    }
}
