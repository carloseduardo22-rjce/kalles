package dev.kalles.sale.cashregister.exception;

public class OperatorAlreadyInSessionException extends RuntimeException {
    public OperatorAlreadyInSessionException(String operatorCode) {
        super("O operador " + operatorCode + " já está vinculado a uma sessão ativa em outro caixa");
    }
}
