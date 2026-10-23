package dev.kalles.client.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class ClientPersonTypeImmutableException extends BusinessRuleViolationException {

    public ClientPersonTypeImmutableException() {
        super("CLIENT_PERSON_TYPE_IMMUTABLE", "O tipo do cliente não muda depois do cadastro. Exclua o cliente e cadastre de novo.");
    }
}
