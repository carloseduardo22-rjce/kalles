package dev.kalles.support.domain;

/**
 * State: OPEN — ticket created by the user, not yet assigned to any agent.
 * <p>
 * Allowed transition:
 *  - assign() → IN_PROGRESS
 */
class OpenState implements TicketState {

    static final OpenState INSTANCE = new OpenState();

    private OpenState() {
    }

    @Override
    public void assign(Ticket ticket, Agent agent) {
        ticket.applyStatus(TicketStatus.IN_PROGRESS);
        ticket.applyAgent(agent);
        ticket.applyInteraction(new Interaction(
                "Ticket assigned to agent " + agent.getName(),
                InteractionType.INTERNAL_NOTE));
        ticket.applyState(InProgressState.INSTANCE);
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
        throw ticket.agentReplyRequiresAssignment();
    }

    @Override
    public void editLastAgentMessage(Ticket ticket, String content) {
        throw ticket.agentReplyRequiresAssignment();
    }

    @Override
    public void close(Ticket ticket) {
        throw ticket.invalidTransition("Invalid state transition: only resolved tickets can be closed");
    }
}
