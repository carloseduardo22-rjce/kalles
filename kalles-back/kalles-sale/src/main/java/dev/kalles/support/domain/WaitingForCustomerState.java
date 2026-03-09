package dev.kalles.support.domain;

import dev.kalles.support.domain.exception.InvalidStateTransitionException;

/**
 * State: WAITING_FOR_CUSTOMER — agent asked a question; SLA is paused.
 * <p>
 * Blocked transition:
 *  - assign() → throws InvalidStateTransitionException
 */
class WaitingForCustomerState implements TicketState {

    static final WaitingForCustomerState INSTANCE = new WaitingForCustomerState();

    private WaitingForCustomerState() {
    }

    @Override
    public void assign(Ticket ticket, Agent agent) {
        throw new InvalidStateTransitionException(
                "Invalid state transition: the ticket is waiting for a customer response");
    }
}
