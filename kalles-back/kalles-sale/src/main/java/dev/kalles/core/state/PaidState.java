package dev.kalles.core.state;

import dev.kalles.core.entity.Sale;

public class PaidState extends AbstractSaleState {

    public static final String NAME = "PAID";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Pagamento confirmado";
    }

    @Override
    public void completeSale(Sale sale) {
        sale.setState(new CompletedState());
    }

    @Override
    public void cancel(Sale sale) {
        // Venda paga mas ainda não concluída: cliente desistiu/erro de cobrança.
        // O estoque ainda não foi deduzido (isso ocorre apenas em completeSale).
        sale.setState(new CanceledState());
    }
}
