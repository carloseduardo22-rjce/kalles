package dev.kalles.support.domain;

import dev.kalles.support.domain.exception.InvalidStateTransitionException;

/**
 * State: IN_PROGRESS — ticket has been assigned to an agent.
 * <p>
 * Blocked transition:
 *  - assign() → throws InvalidStateTransitionException
 */
class InProgressState implements TicketState {

    static final InProgressState INSTANCE = new InProgressState();

    private InProgressState() {
    }

    @Override
    public void assign(Ticket ticket, Agent agent) {
        throw new InvalidStateTransitionException(
                "Invalid state transition: the ticket is already in progress");
    }
}
