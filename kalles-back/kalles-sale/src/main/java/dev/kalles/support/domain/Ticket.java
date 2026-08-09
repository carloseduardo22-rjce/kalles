package dev.kalles.support.domain;

import dev.kalles.support.exception.InvalidStateTransitionException;
import dev.kalles.support.exception.TicketDomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
    private final List<Interaction> interactions;

    private Ticket() {
        this.interactions = new ArrayList<>();
    }

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

    public static Ticket reconstitute(
            String id,
            String title,
            String description,
            TicketStatus status,
            Priority priority,
            User user,
            Agent agent,
            Category category,
            Sla sla,
            List<Interaction> interactions
    ) {
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

    public static Ticket reconstitute(
            String id,
            String title,
            String description,
            TicketStatus status,
            Priority priority,
            User user,
            Agent agent,
            Category category
    ) {
        Ticket ticket = new Ticket();
        ticket.id = id;
        ticket.title = title;
        ticket.description = description;
        ticket.status = status;
        ticket.priority = priority;
        ticket.user = user;
        ticket.agent = agent;
        ticket.category = category;
        ticket.sla = resolveSla(status);
        ticket.state = resolveState(status);
        return ticket;
    }

    public void assign(Agent agent) {
        this.state.assign(this, agent);
    }

    public void addCustomerMessage(String content) {
        this.state.addCustomerMessage(this, content);
    }

    public void editLastCustomerMessage(String content) {
        this.state.editLastCustomerMessage(this, content);
    }

    public void addAgentMessage(String content, boolean markAsResolved) {
        this.state.addAgentMessage(this, content, markAsResolved);
    }

    public void editLastAgentMessage(String content) {
        this.state.editLastAgentMessage(this, content);
    }

    public void close() {
        this.state.close(this);
    }

    public List<Interaction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    void applyStatus(TicketStatus newStatus) {
        this.status = newStatus;
    }

    void applyAgent(Agent agent) {
        this.agent = agent;
    }

    void applyInteraction(Interaction interaction) {
        this.interactions.add(interaction);
    }

    void replaceInteraction(Interaction updatedInteraction) {
        for (int index = interactions.size() - 1; index >= 0; index--) {
            Interaction current = interactions.get(index);
            if (Objects.equals(current.getId(), updatedInteraction.getId())) {
                interactions.set(index, updatedInteraction);
                return;
            }
        }
        throw new TicketDomainException("Latest interaction could not be updated");
    }

    void applyState(TicketState newState) {
        this.state = newState;
    }

    void appendConversationMessage(String content, InteractionType expectedType) {
        requireNonBlank(content, "Message content is required");
        ensureCanAppendConversationMessage(expectedType);
        applyInteraction(new Interaction(content.strip(), expectedType));
    }

    void editLatestConversationMessage(String content, InteractionType expectedType) {
        requireNonBlank(content, "Message content is required");
        Interaction latest = latestConversationInteraction()
                .orElseThrow(() -> new TicketDomainException("There is no message available to edit"));

        if (latest.getType() != expectedType) {
            throw new TicketDomainException("Only the latest message from the same author can be edited");
        }

        replaceInteraction(Interaction.reconstitute(
                latest.getId(),
                content.strip(),
                latest.getType(),
                latest.getCreatedAt()
        ));
    }

    void ensureLatestConversationMessageFrom(InteractionType expectedType, String message) {
        Interaction latest = latestConversationInteraction()
                .orElseThrow(() -> new InvalidStateTransitionException(message));
        if (latest.getType() != expectedType) {
            throw new InvalidStateTransitionException(message);
        }
    }

    void ensureAssignedAgent() {
        if (agent == null) {
            throw agentReplyRequiresAssignment();
        }
    }

    InvalidStateTransitionException invalidTransition(String message) {
        return new InvalidStateTransitionException(message);
    }

    TicketDomainException agentReplyRequiresAssignment() {
        return new TicketDomainException("The ticket must be assigned before an agent can reply");
    }

    private void ensureCanAppendConversationMessage(InteractionType type) {
        latestConversationInteraction().ifPresent(latest -> {
            if (latest.getType() == type) {
                throw new TicketDomainException("You must edit your latest message before sending another one");
            }
        });
    }

    private Optional<Interaction> latestConversationInteraction() {
        return interactions.stream()
                .filter(interaction -> interaction.getType() != InteractionType.INTERNAL_NOTE)
                .max(Comparator.comparing(Interaction::getCreatedAt));
    }

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

    private static Sla resolveSla(TicketStatus status) {
        return (status == TicketStatus.OPEN || status == TicketStatus.IN_PROGRESS)
                ? Sla.start()
                : Sla.inactive();
    }
}
