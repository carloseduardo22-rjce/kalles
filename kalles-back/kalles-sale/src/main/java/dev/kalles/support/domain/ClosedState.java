package dev.kalles.support.domain;

import dev.kalles.support.domain.exception.InvalidStateTransitionException;

/**
 * State: CLOSED — ticket permanently closed.
 * No transitions are allowed from this state.
 */
class ClosedState implements TicketState {

    static final ClosedState INSTANCE = new ClosedState();

    private ClosedState() {
    }

    @Override
    public void assign(Ticket ticket, Agent agent) {
        throw new InvalidStateTransitionException(
                "Invalid state transition: closed tickets cannot be reopened through this flow");
    }
}
