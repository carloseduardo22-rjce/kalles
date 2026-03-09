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
}
