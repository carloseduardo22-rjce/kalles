package dev.kalles.fidelity.exception;

import dev.kalles.shared.exception.ConflictException;

public class ClientAlreadyInFidelityException extends ConflictException {

    public ClientAlreadyInFidelityException() {
        super("CLIENT_ALREADY_IN_FIDELITY", "Cliente já está inserido no programa de fidelidade.");
    }
}
