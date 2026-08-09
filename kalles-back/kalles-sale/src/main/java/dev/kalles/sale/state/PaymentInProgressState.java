package dev.kalles.sale.state;

import dev.kalles.sale.entity.Sale;

public class PaymentInProgressState extends AbstractSaleState {

    public static final String NAME = "PAYMENT_IN_PROGRESS";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Pagamento iniciado";
    }

    @Override
    public void finishPayment(Sale sale) {
        sale.setState(new PaidState());
    }

    @Override
    public void cancel(Sale sale) {
        sale.setState(new CanceledState());
    }

    @Override
    public void resume(Sale sale) {
        sale.setState(new OpenState());
    }
}
