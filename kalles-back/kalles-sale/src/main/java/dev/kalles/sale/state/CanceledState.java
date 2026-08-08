package dev.kalles.sale.state;

public class CanceledState extends AbstractSaleState {

    public static final String NAME = "CANCELED";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Venda cancelada";
    }

}
