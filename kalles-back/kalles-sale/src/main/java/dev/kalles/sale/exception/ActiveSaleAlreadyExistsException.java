package dev.kalles.sale.exception;

import dev.kalles.shared.exception.ConflictException;

public class ActiveSaleAlreadyExistsException extends ConflictException {

    public ActiveSaleAlreadyExistsException(Throwable cause) {
        super("ACTIVE_SALE_ALREADY_EXISTS", "Já existe uma venda ativa para esta sessão. Recarregue a venda atual.", cause);
    }
}
