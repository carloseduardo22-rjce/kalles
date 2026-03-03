package dev.kalles.sale.cashregister.exception;

public class ActiveSessionAlreadyExistsException extends RuntimeException {
    public ActiveSessionAlreadyExistsException(String cashRegisterCode) {
        super("O caixa " + cashRegisterCode + " já possui uma sessão ativa");
    }
}
