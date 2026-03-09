package dev.kalles.support.domain;

import dev.kalles.support.domain.exception.TicketDomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Root aggregate of the Support domain.
 * <p>
 * All state mutations are delegated to the current {@link TicketState} object,
 * which implements the State Pattern to enforce that only valid transitions
 * of the state machine can be executed.
 * <p>
 * Invariants enforced by this class:
 * <ul>
 *   <li>Title, description and category are mandatory when opening a ticket.</li>
 *   <li>Priority is automatically derived from the Category.</li>
 *   <li>Initial status is always OPEN.</li>
 *   <li>The SLA is automatically started upon opening.</li>
 *   <li>Internal mutations are package-private (accessible to state classes only).</li>
 * </ul>
 */
@Getter
public class Ticket {

    private String id;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private User user;
    private Agent agent;
    private Category category;
    private Sla sla;

    @Getter(lombok.AccessLevel.NONE)
    private TicketState state;

    @Getter(lombok.AccessLevel.NONE)
    private List<Interaction> interactions;

    private Ticket() {
        this.interactions = new ArrayList<>();
    }

    // ---------------------------------------------------------------
    // Factory Methods
    // ---------------------------------------------------------------

    /**
     * Opens a new Ticket, enforcing all business invariants.
     * Priority is automatically derived from the provided {@link Category}.
     */
    public static Ticket open(String title, String description, User user, Category category) {
        requireNonBlank(title, "Ticket title is required");
        requireNonBlank(description, "Ticket description is required");
        requireNonNull(category, "Ticket category is required");

        Ticket ticket = new Ticket();
        ticket.id = UUID.randomUUID().toString();
        ticket.title = title.strip();
        ticket.description = description.strip();
        ticket.user = user;
        ticket.category = category;
        ticket.priority = category.getDefaultPriority();
        ticket.status = TicketStatus.OPEN;
        ticket.sla = Sla.start();
        ticket.state = OpenState.INSTANCE;
        return ticket;
    }

    /**
     * Reconstitutes a Ticket from persisted data including its interactions.
     * No business validation is applied — data was validated at creation time.
     */
    public static Ticket reconstitute(String id, String title, String description,
                                      TicketStatus status, Priority priority,
                                      User user, Agent agent, Category category,
                                      Sla sla, List<Interaction> interactions) {
        Ticket ticket = new Ticket();
        ticket.id = id;
        ticket.title = title;
        ticket.description = description;
        ticket.status = status;
        ticket.priority = priority;
        ticket.user = user;
        ticket.agent = agent;
        ticket.category = category;
        ticket.sla = sla;
        ticket.state = resolveState(status);
        ticket.interactions.addAll(interactions);
        return ticket;
    }

    /**
     * Reconstitutes a Ticket from persisted data.
     * No business validation is applied — data was validated at creation time.
     * The state object is resolved from the given status.
     */
    public static Ticket reconstitute(String id, String title, String description,
                                      TicketStatus status, Priority priority,
                                      User user, Agent agent, Category category) {
        Ticket ticket = new Ticket();
        ticket.id = id;
        ticket.title = title;
        ticket.description = description;
        ticket.status = status;
        ticket.priority = priority;
        ticket.user = user;
        ticket.agent = agent;
        ticket.category = category;
        ticket.sla = resolveSlа(status);
        ticket.state = resolveState(status);
        return ticket;
    }

    // ---------------------------------------------------------------
    // Behaviours / Commands
    // ---------------------------------------------------------------

    /**
     * An Agent takes responsibility for this Ticket.
     * Delegates to the current state to validate the transition.
     */
    public void assign(Agent agent) {
        this.state.assign(this, agent);
    }

    // ---------------------------------------------------------------
    // Getter — returns an unmodifiable view of interactions
    // ---------------------------------------------------------------

    public List<Interaction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    // ---------------------------------------------------------------
    // Package-private mutations — used exclusively by state classes
    // ---------------------------------------------------------------

    void applyStatus(TicketStatus newStatus) {
        this.status = newStatus;
    }

    void applyAgent(Agent agent) {
        this.agent = agent;
    }

    void applyInteraction(Interaction interaction) {
        this.interactions.add(interaction);
    }

    void applyState(TicketState newState) {
        this.state = newState;
    }

    // ---------------------------------------------------------------
    // Private validation helpers
    // ---------------------------------------------------------------

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new TicketDomainException(message);
        }
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new TicketDomainException(message);
        }
    }

    private static TicketState resolveState(TicketStatus status) {
        return switch (status) {
            case OPEN -> OpenState.INSTANCE;
            case IN_PROGRESS -> InProgressState.INSTANCE;
            case WAITING_FOR_CUSTOMER -> WaitingForCustomerState.INSTANCE;
            case RESOLVED -> ResolvedState.INSTANCE;
            case CLOSED -> ClosedState.INSTANCE;
        };
    }

    private static Sla resolveSlа(TicketStatus status) {
        return (status == TicketStatus.OPEN || status == TicketStatus.IN_PROGRESS)
                ? Sla.start()
                : Sla.inactive();
    }
}
