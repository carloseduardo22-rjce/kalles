package dev.kalles.support.domain;

import dev.kalles.support.domain.exception.InvalidStateTransitionException;

/**
 * State: RESOLVED — agent has provided a solution; awaiting customer confirmation.
 * <p>
 * Blocked transition:
 *  - assign() → throws InvalidStateTransitionException
 */
class ResolvedState implements TicketState {

    static final ResolvedState INSTANCE = new ResolvedState();

    private ResolvedState() {
    }

    @Override
    public void assign(Ticket ticket, Agent agent) {
        throw new InvalidStateTransitionException(
                "Invalid state transition: the ticket has already been resolved");
    }
}
