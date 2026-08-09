package dev.kalles.support.domain;

import dev.kalles.support.exception.InvalidStateTransitionException;

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

    @Override
    public void addCustomerMessage(Ticket ticket, String content) {
        ticket.appendConversationMessage(content, InteractionType.CUSTOMER_MESSAGE);
        ticket.applyStatus(TicketStatus.IN_PROGRESS);
        ticket.applyState(InProgressState.INSTANCE);
    }

    @Override
    public void editLastCustomerMessage(Ticket ticket, String content) {
        ticket.editLatestConversationMessage(content, InteractionType.CUSTOMER_MESSAGE);
    }

    @Override
    public void addAgentMessage(Ticket ticket, String content, boolean markAsResolved) {
        ticket.ensureAssignedAgent();
        ticket.appendConversationMessage(content, InteractionType.AGENT_MESSAGE);
        ticket.applyStatus(markAsResolved ? TicketStatus.RESOLVED : TicketStatus.WAITING_FOR_CUSTOMER);
        ticket.applyState(markAsResolved ? ResolvedState.INSTANCE : WaitingForCustomerState.INSTANCE);
    }

    @Override
    public void editLastAgentMessage(Ticket ticket, String content) {
        ticket.ensureAssignedAgent();
        ticket.editLatestConversationMessage(content, InteractionType.AGENT_MESSAGE);
    }

    @Override
    public void close(Ticket ticket) {
        throw ticket.invalidTransition("Invalid state transition: only resolved tickets can be closed");
    }
}
