package dev.kalles.sale.state;

public class CompletedState extends AbstractSaleState {

    public static final String NAME = "COMPLETED";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Venda concluída";
    }

}
