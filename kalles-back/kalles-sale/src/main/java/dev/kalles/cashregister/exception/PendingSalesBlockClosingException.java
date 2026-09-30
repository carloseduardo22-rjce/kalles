package dev.kalles.cashregister.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class PendingSalesBlockClosingException extends BusinessRuleViolationException {

    public PendingSalesBlockClosingException(long pendingSales) {
        super("CASH_REGISTER_PENDING_SALES",
                "Nao e possivel fechar o caixa: existem " + pendingSales
                        + " venda(s) pendente(s) (em andamento, em pagamento ou pagas sem conclusao)."
                        + " Conclua ou cancele as vendas antes de fechar a sessao.");
    }
}
