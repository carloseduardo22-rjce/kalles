package dev.kalles.client.exception;

import dev.kalles.shared.exception.ConflictException;

public class ClientDocumentAlreadyExistsException extends ConflictException {

    public ClientDocumentAlreadyExistsException() {
        super("CLIENT_DOCUMENT_ALREADY_EXISTS", "Já existe um cliente com o documento informado nesta filial.");
    }
}
