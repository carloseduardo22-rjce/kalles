package dev.kalles.support.domain;

import dev.kalles.support.exception.InvalidStateTransitionException;

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
    }

    @Override
    public void editLastAgentMessage(Ticket ticket, String content) {
        ticket.ensureAssignedAgent();
        ticket.editLatestConversationMessage(content, InteractionType.AGENT_MESSAGE);
    }

    @Override
    public void close(Ticket ticket) {
        ticket.ensureLatestConversationMessageFrom(
                InteractionType.CUSTOMER_MESSAGE,
                "Invalid state transition: the customer must reply before closing the ticket"
        );
        ticket.applyStatus(TicketStatus.CLOSED);
        ticket.applyState(ClosedState.INSTANCE);
    }
}
