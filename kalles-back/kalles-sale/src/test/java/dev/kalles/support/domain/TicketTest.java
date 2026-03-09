package dev.kalles.support.domain;

import dev.kalles.support.domain.exception.InvalidStateTransitionException;
import dev.kalles.support.domain.exception.TicketDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Ticket — Domain Tests")
class TicketTest {

    // ---------------------------------------------------------------
    // Common fixtures
    // ---------------------------------------------------------------
    private Category bugCategory;
    private User     user;
    private Agent    agent;

    @BeforeEach
    void setUp() {
        bugCategory = new Category("System", "Bug", Priority.HIGH);
        user        = new User("joao.silva@cliente.com", "João Silva");
        agent       = new Agent("ATD-001", "Carlos Dev");
    }

    // ===============================================================
    // Block 1 — Opening a Ticket
    // ===============================================================

    @Nested
    @DisplayName("Opening a Ticket")
    class OpeningATicket {

        @Test
        @DisplayName("Should create ticket with status OPEN, category-derived priority and active SLA")
        void shouldCreateTicketSuccessfully() {
            Ticket ticket = Ticket.open("Error 500 in PDV", "Detailed error description", user, bugCategory);

            assertThat(ticket.getTitle()).isEqualTo("Error 500 in PDV");
            assertThat(ticket.getDescription()).isEqualTo("Detailed error description");
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(ticket.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(ticket.getCategory()).isEqualTo(bugCategory);
            assertThat(ticket.getUser()).isEqualTo(user);
            assertThat(ticket.getAgent()).isNull();
            assertThat(ticket.getSla().isActive()).isTrue();
            assertThat(ticket.getInteractions()).isEmpty();
        }

        @Test
        @DisplayName("Should reject opening a ticket without a title")
        void shouldRejectNullTitle() {
            assertThatThrownBy(() -> Ticket.open(null, "Description", user, bugCategory))
                    .isInstanceOf(TicketDomainException.class)
                    .hasMessage("Ticket title is required");
        }

        @Test
        @DisplayName("Should reject opening a ticket with a blank title")
        void shouldRejectBlankTitle() {
            assertThatThrownBy(() -> Ticket.open("   ", "Description", user, bugCategory))
                    .isInstanceOf(TicketDomainException.class)
                    .hasMessage("Ticket title is required");
        }

        @Test
        @DisplayName("Should reject opening a ticket without a description")
        void shouldRejectNullDescription() {
            assertThatThrownBy(() -> Ticket.open("Title", null, user, bugCategory))
                    .isInstanceOf(TicketDomainException.class)
                    .hasMessage("Ticket description is required");
        }

        @Test
        @DisplayName("Should reject opening a ticket without a category")
        void shouldRejectNullCategory() {
            assertThatThrownBy(() -> Ticket.open("Title", "Description", user, null))
                    .isInstanceOf(TicketDomainException.class)
                    .hasMessage("Ticket category is required");
        }

        @Test
        @DisplayName("Priority should be automatically derived from the category")
        void shouldDeriveDefaultPriorityFromCategory() {
            Category mediumCategory = new Category("Finance", "Question", Priority.MEDIUM);

            Ticket ticket = Ticket.open("Title", "Description", user, mediumCategory);

            assertThat(ticket.getPriority()).isEqualTo(Priority.MEDIUM);
        }
    }

    // ===============================================================
    // Block 2 — Transition: OPEN → IN_PROGRESS
    // ===============================================================

    @Nested
    @DisplayName("Transition: OPEN → IN_PROGRESS")
    class TransitionOpenToInProgress {

        private Ticket openTicket;

        @BeforeEach
        void createOpenTicket() {
            openTicket = Ticket.open("Title", "Description", user, bugCategory);
        }

        @Test
        @DisplayName("Status should change to IN_PROGRESS when an agent assigns the ticket")
        void shouldChangeStatusToInProgress() {
            openTicket.assign(agent);

            assertThat(openTicket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("The agent should be linked to the ticket upon assignment")
        void shouldLinkAgentToTicket() {
            openTicket.assign(agent);

            assertThat(openTicket.getAgent()).isEqualTo(agent);
        }

        @Test
        @DisplayName("Should automatically record an INTERNAL_NOTE when agent assigns")
        void shouldRecordInternalNoteOnAssignment() {
            openTicket.assign(agent);

            assertThat(openTicket.getInteractions()).hasSize(1);

            Interaction note = openTicket.getInteractions().get(0);
            assertThat(note.getType()).isEqualTo(InteractionType.INTERNAL_NOTE);
            assertThat(note.getContent()).isEqualTo("Ticket assigned to agent Carlos Dev");
        }

        @Test
        @DisplayName("Should reject assigning a ticket that is already IN_PROGRESS")
        void shouldRejectAssigningAlreadyInProgressTicket() {
            openTicket.assign(agent);
            Agent anotherAgent = new Agent("ATD-002", "Second Agent");

            assertThatThrownBy(() -> openTicket.assign(anotherAgent))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Invalid state transition: the ticket is already in progress");
        }

        @Test
        @DisplayName("Should reject assigning a CLOSED ticket")
        void shouldRejectAssigningClosedTicket() {
            Ticket closedTicket = Ticket.reconstitute(
                    "T-003", "Title", "Description",
                    TicketStatus.CLOSED, Priority.HIGH, user, null, bugCategory);

            assertThatThrownBy(() -> closedTicket.assign(agent))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Invalid state transition: closed tickets cannot be reopened through this flow");
        }

        @Test
        @DisplayName("Should reject assigning a RESOLVED ticket")
        void shouldRejectAssigningResolvedTicket() {
            Ticket resolvedTicket = Ticket.reconstitute(
                    "T-004", "Title", "Description",
                    TicketStatus.RESOLVED, Priority.HIGH, user, null, bugCategory);

            assertThatThrownBy(() -> resolvedTicket.assign(agent))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Invalid state transition: the ticket has already been resolved");
        }
    }
}
