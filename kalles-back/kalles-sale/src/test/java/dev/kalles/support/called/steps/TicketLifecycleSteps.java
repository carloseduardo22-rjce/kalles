package dev.kalles.support.called.steps;

import dev.kalles.support.domain.Agent;
import dev.kalles.support.domain.Category;
import dev.kalles.support.domain.Interaction;
import dev.kalles.support.domain.InteractionType;
import dev.kalles.support.domain.Priority;
import dev.kalles.support.domain.Ticket;
import dev.kalles.support.domain.TicketStatus;
import dev.kalles.support.domain.User;
import dev.kalles.support.domain.exception.TicketDomainException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step Definitions for ciclo_de_vida_chamado.feature.
 * <p>
 * A new instance is created per scenario by Cucumber, so all fields
 * are naturally isolated between scenarios.
 */
public class TicketLifecycleSteps {

    // ---------------------------------------------------------------
    // Scenario-scoped state
    // ---------------------------------------------------------------
    private final Map<String, Category> categories = new HashMap<>();
    private final Map<String, User>     users      = new HashMap<>();
    private final Map<String, Agent>    agents     = new HashMap<>();
    private final Map<String, Ticket>   tickets    = new HashMap<>();

    private Ticket    lastCreatedTicket;
    private Exception lastException;

    // ===============================================================
    // Background — runs before each scenario
    // ===============================================================

    @Given("que existe uma regra de negócio onde a categoria {string} e subcategoria {string} gera prioridade {string}")
    public void categoryRuleExists(String categoryName, String subcategory, String priority) {
        Category category = new Category(categoryName, subcategory, Priority.valueOf(priority));
        categories.put(categoryKey(categoryName, subcategory), category);
    }

    @And("que existe um usuário com email {string} e nome {string}")
    public void userExists(String email, String name) {
        users.put(email, new User(email, name));
    }

    @And("que existe um atendente com matrícula {string} e nome {string}")
    public void agentExists(String employeeId, String name) {
        agents.put(employeeId, new Agent(employeeId, name));
    }

    // ===============================================================
    // Block 1 — Opening a Ticket
    // ===============================================================

    @When("o usuário {string} abre um novo chamado com os seguintes dados:")
    public void userOpensTicket(String email, Map<String, String> data) {
        User     user     = users.get(email);
        Category category = categories.get(categoryKey(data.get("categoria"), data.get("subcategoria")));
        try {
            lastCreatedTicket = Ticket.open(data.get("titulo"), data.get("descricao"), user, category);
        } catch (TicketDomainException e) {
            lastException = e;
        }
    }

    @Then("um chamado deve ser criado com sucesso")
    public void ticketShouldBeCreated() {
        assertThat(lastException).isNull();
        assertThat(lastCreatedTicket).isNotNull();
    }

    @And("o chamado deve ter o status {string}")
    public void ticketShouldHaveStatus(String expectedStatus) {
        assertThat(lastCreatedTicket.getStatus())
                .isEqualTo(TicketStatus.valueOf(expectedStatus));
    }

    @And("o chamado deve estar associado ao usuário {string}")
    public void ticketShouldBelongToUser(String email) {
        assertThat(lastCreatedTicket.getUser().getEmail()).isEqualTo(email);
    }

    @And("o sistema deve atribuir automaticamente a prioridade {string} devido à classificação {string}")
    public void ticketShouldHaveAutomaticPriority(String expectedPriority, String classification) {
        assertThat(lastCreatedTicket.getPriority())
                .isEqualTo(Priority.valueOf(expectedPriority));
    }

    @And("o chamado não deve ter nenhum atendente atribuído")
    public void ticketShouldHaveNoAgent() {
        assertThat(lastCreatedTicket.getAgent()).isNull();
    }

    @And("o SLA do chamado deve estar ativo com o contador de tempo iniciado")
    public void slaShouldBeActive() {
        assertThat(lastCreatedTicket.getSla().isActive()).isTrue();
    }

    // ---------------------------------------------------------------
    // Rejection scenarios
    // ---------------------------------------------------------------

    @When("o usuário {string} tenta abrir um chamado sem informar o título")
    public void userTriesToOpenWithoutTitle(String email) {
        tryToOpenTicket(email, null, "Valid description for the test");
    }

    @When("o usuário {string} tenta abrir um chamado sem informar a categoria")
    public void userTriesToOpenWithoutCategory(String email) {
        User user = users.get(email);
        try {
            lastCreatedTicket = Ticket.open("Valid title for the test", "Valid description for the test", user, null);
        } catch (TicketDomainException e) {
            lastException = e;
        }
    }

    @When("o usuário {string} tenta abrir um chamado sem informar a descrição")
    public void userTriesToOpenWithoutDescription(String email) {
        tryToOpenTicket(email, "Valid title for the test", null);
    }

    @Then("o sistema deve recusar a operação")
    public void systemShouldRejectOperation() {
        assertThat(lastException)
                .as("A domain exception was expected but none was thrown.")
                .isNotNull();
    }

    @And("a mensagem de erro deve ser {string}")
    public void errorMessageShouldBe(String expectedMessage) {
        assertThat(lastException).hasMessage(expectedMessage);
    }

    // ===============================================================
    // Block 2 — Transition OPEN → IN_PROGRESS
    // ===============================================================

    @Given("que existe um chamado de id {string} com status {string} pertencente ao usuário {string}")
    public void openTicketExistsForUser(String id, String status, String email) {
        User     user     = users.get(email);
        Category category = categories.values().iterator().next();
        Ticket   ticket   = Ticket.reconstitute(id, "Test title", "Test description",
                TicketStatus.valueOf(status), Priority.HIGH, user, null, category);
        tickets.put(id, ticket);
    }

    @Given("que existe um chamado de id {string} com status {string} sob responsabilidade do atendente {string}")
    public void inProgressTicketExistsUnderAgent(String id, String status, String employeeId) {
        User     user     = users.values().iterator().next();
        Category category = categories.values().iterator().next();
        Agent    agent    = agents.get(employeeId);
        Ticket   ticket   = Ticket.reconstitute(id, "Test title", "Test description",
                TicketStatus.valueOf(status), Priority.HIGH, user, agent, category);
        tickets.put(id, ticket);
    }

    @Given("que existe um chamado de id {string} com status {string}")
    public void ticketExistsWithStatus(String id, String status) {
        User     user     = users.values().iterator().next();
        Category category = categories.values().iterator().next();
        Ticket   ticket   = Ticket.reconstitute(id, "Test title", "Test description",
                TicketStatus.valueOf(status), Priority.HIGH, user, null, category);
        tickets.put(id, ticket);
    }

    @When("o atendente {string} assume o chamado {string}")
    public void agentAssignsTicket(String employeeId, String ticketId) {
        Agent  agent  = agents.get(employeeId);
        Ticket ticket = tickets.get(ticketId);
        try {
            ticket.assign(agent);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("o status do chamado {string} deve ser alterado para {string}")
    public void ticketStatusShouldBe(String ticketId, String expectedStatus) {
        assertThat(tickets.get(ticketId).getStatus())
                .isEqualTo(TicketStatus.valueOf(expectedStatus));
    }

    @And("o atendente {string} deve ser atribuído como responsável pelo chamado {string}")
    public void agentShouldBeAssigned(String employeeId, String ticketId) {
        assertThat(tickets.get(ticketId).getAgent().getEmployeeId()).isEqualTo(employeeId);
    }

    @And("uma nota interna deve ser registrada automaticamente no chamado {string} com o texto {string}")
    public void internalNoteShouldBeRecorded(String ticketId, String expectedText) {
        Ticket ticket = tickets.get(ticketId);
        assertThat(ticket.getInteractions())
                .isNotEmpty()
                .anySatisfy(interaction -> {
                    assertThat(interaction.getType()).isEqualTo(InteractionType.INTERNAL_NOTE);
                    assertThat(interaction.getContent()).isEqualTo(expectedText);
                });
    }

    @When("um segundo atendente com matrícula {string} tenta assumir o chamado {string}")
    public void secondAgentTriesToAssign(String employeeId, String ticketId) {
        Agent  secondAgent = new Agent(employeeId, "Second Agent");
        Ticket ticket      = tickets.get(ticketId);
        try {
            ticket.assign(secondAgent);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("o atendente {string} tenta assumir o chamado {string}")
    public void agentTriesToAssign(String employeeId, String ticketId) {
        Agent  agent  = agents.get(employeeId);
        Ticket ticket = tickets.get(ticketId);
        try {
            ticket.assign(agent);
        } catch (Exception e) {
            lastException = e;
        }
    }

    // ===============================================================
    // Private helpers
    // ===============================================================

    private void tryToOpenTicket(String email, String title, String description) {
        User     user     = users.get(email);
        Category category = categories.values().iterator().next();
        try {
            lastCreatedTicket = Ticket.open(title, description, user, category);
        } catch (TicketDomainException e) {
            lastException = e;
        }
    }

    private String categoryKey(String name, String subcategory) {
        return name + "/" + subcategory;
    }
}
