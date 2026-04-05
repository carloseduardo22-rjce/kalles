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

    @Override
    public void addCustomerMessage(Ticket ticket, String content) {
        throw ticket.invalidTransition("Invalid state transition: closed tickets cannot receive new messages");
    }

    @Override
    public void editLastCustomerMessage(Ticket ticket, String content) {
        throw ticket.invalidTransition("Invalid state transition: closed tickets cannot be edited");
    }

    @Override
    public void addAgentMessage(Ticket ticket, String content, boolean markAsResolved) {
        throw ticket.invalidTransition("Invalid state transition: closed tickets cannot receive new messages");
    }

    @Override
    public void editLastAgentMessage(Ticket ticket, String content) {
        throw ticket.invalidTransition("Invalid state transition: closed tickets cannot be edited");
    }

    @Override
    public void close(Ticket ticket) {
        throw ticket.invalidTransition("Invalid state transition: the ticket is already closed");
    }
}
