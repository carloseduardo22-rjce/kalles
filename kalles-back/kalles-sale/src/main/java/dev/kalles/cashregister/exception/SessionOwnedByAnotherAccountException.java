package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.ForbiddenOperationException;

public class SessionOwnedByAnotherAccountException extends ForbiddenOperationException {

    public SessionOwnedByAnotherAccountException() {
        super("SESSION_OWNED_BY_ANOTHER_ACCOUNT", "Só quem abriu a sessão pode fechá-la pelo PDV.");
    }
}
