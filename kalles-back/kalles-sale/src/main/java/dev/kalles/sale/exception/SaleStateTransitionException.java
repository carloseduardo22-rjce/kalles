package dev.kalles.sale.exception;

import dev.kalles.shared.exception.ConflictException;

public class SaleStateTransitionException extends ConflictException {

    public SaleStateTransitionException(String action, String stateDescription) {
        super("SALE_INVALID_STATE_TRANSITION", "Não é possível " + action + " no estado: " + stateDescription);
    }
}
