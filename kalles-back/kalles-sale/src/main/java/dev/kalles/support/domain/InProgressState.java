package dev.kalles.support.domain;

import dev.kalles.support.exception.InvalidStateTransitionException;

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

    @Override
    public void addCustomerMessage(Ticket ticket, String content) {
        ticket.appendConversationMessage(content, InteractionType.CUSTOMER_MESSAGE);
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
