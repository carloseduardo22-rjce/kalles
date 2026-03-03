package dev.kalles.sale.core.state;

import dev.kalles.sale.core.entity.Sale;

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
}
