package dev.kalles.sale.core.state;

public class SaleStateFactory {

    private SaleStateFactory() {
       
    }

    public static SaleState fromName(String stateName) {
        if (stateName == null) {
            return new OpenState(); 
        }

        return switch (stateName) {
            case OpenState.NAME -> new OpenState();
            case OnHoldState.NAME -> new OnHoldState();
            case PaymentInProgressState.NAME -> new PaymentInProgressState();
            case PaidState.NAME -> new PaidState();
            case CanceledState.NAME -> new CanceledState();
            case CompletedState.NAME -> new CompletedState();
            default -> throw new IllegalArgumentException("Estado desconhecido: " + stateName);
        };
    }
}
