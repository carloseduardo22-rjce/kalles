package dev.kalles.sale.core.state;

import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Sale;

public class OnHoldState extends AbstractSaleState {

    public static final String NAME = "ON_HOLD";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Cliente saiu momentaneamente";
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
    public void resume(Sale sale) {
        sale.setState(new OpenState());
    }

    @Override
    public void cancel(Sale sale) {
        sale.setState(new CanceledState());
    }
}
