package dev.kalles.client.exception;

import dev.kalles.shared.exception.ConflictException;

public class ClientCpfAlreadyExistsException extends ConflictException {

    public ClientCpfAlreadyExistsException() {
        super("CLIENT_CPF_ALREADY_EXISTS", "Já existe um cliente com o CPF informado nesta filial.");
    }
}
