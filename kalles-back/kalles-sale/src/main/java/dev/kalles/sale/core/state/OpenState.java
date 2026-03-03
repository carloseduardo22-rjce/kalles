package dev.kalles.sale.core.state;

import java.math.BigDecimal;
import java.util.UUID;

import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Sale;

public class OpenState extends AbstractSaleState {

    public static final String NAME = "OPEN";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Venda em andamento";
    }

    @Override
    public void addItem(Sale sale, Product product) {
        sale.doAddItem(product);
    }

    @Override
    public void removeItem(Sale sale, Product product) {
        sale.doRemoveItem(product);
    }

    @Override
    public void applyItemDiscount(Sale sale, UUID itemId, BigDecimal discountAmount) {
        sale.doApplyItemDiscount(itemId, discountAmount);
    }

    @Override
    public void startPayment(Sale sale) {
        if (sale.getItems().isEmpty()) {
            throw new IllegalStateException("Não é possível iniciar pagamento sem itens na venda.");
        }
        sale.doStartPayment();
        sale.setState(new PaymentInProgressState());
    }

    @Override
    public void hold(Sale sale) {
        sale.setState(new OnHoldState());
    }

    @Override
    public void cancel(Sale sale) {
        sale.setState(new CanceledState());
    }
}
