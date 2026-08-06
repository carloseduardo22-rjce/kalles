package dev.kalles.sale.goal.steps;

import dev.kalles.sale.goal.enums.GoalStatus;
import dev.kalles.sale.goal.enums.Periodicity;
import dev.kalles.sale.payment.support.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GoalSteps extends GoalCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;
    private UUID foreignGoalId;

    @Before
    public void beforeScenario() {
        resetGoalScenario();
        authContext = null;
        response = null;
        foreignGoalId = null;
    }

    @Dado("um admin autenticado para goals")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
    }

    @Dado("uma meta ativa cadastrada em outra filial")
    public void givenGoalInAnotherCompany() {
        foreignGoalId = seedGoal(
                companyBId,
                "12000.00",
                Periodicity.MONTHLY,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                GoalStatus.ACTIVE
        ).getId();
    }

    @Quando("ele cadastrar uma meta na filial ativa")
    public void whenCreatingGoalInActiveCompany() {
        response = givenAuthenticated(authContext, companyAId)
                .body(Map.of(
                        "targetValue", 10000.00,
                        "periodicity", "MONTHLY",
                        "startDate", "2026-04-01",
                        "endDate", "2026-04-30"
                ))
                .when()
                .post("/api/goals");
    }

    @Quando("ele listar metas sem informar a filial ativa")
    public void whenListingGoalsWithoutCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/goals",
                Map.of("Cookie", "kalles_auth_token=" + authContext.authCookie())
        );
    }

    @Quando("ele consultar a meta externa no contexto da filial ativa")
    public void whenFetchingForeignGoalFromActiveCompany() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/goals/" + foreignGoalId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Entao("a resposta de goal deve ter status HTTP {int}")
    public void thenGoalResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a meta criada deve retornar o status {string}")
    public void thenCreatedGoalShouldReturnStatus(String status) {
        assertThat(response.jsonPath().getString("status")).isEqualTo(status);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para goals")
    public void thenGoalShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }
}
