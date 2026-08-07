package dev.kalles.operator.steps;

import dev.kalles.core.enums.operator.PermissionLevel;
import dev.kalles.payment.support.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OperatorSteps extends OperatorCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;
    private UUID foreignOperatorId;

    @Before
    public void beforeScenario() {
        resetOperatorScenario();
        authContext = null;
        response = null;
        foreignOperatorId = null;
    }

    @Dado("um admin autenticado para operators")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
    }

    @Dado("um operador cadastrado em outra filial")
    public void givenOperatorInAnotherCompany() {
        foreignOperatorId = seedOperator(companyBId, "Carlos", "carlos", PermissionLevel.MANAGER, true).getId();
    }

    @Quando("ele cadastrar um operador na filial ativa")
    public void whenCreatingOperatorInActiveCompany() {
        response = givenAuthenticated(authContext, companyAId)
                .body(Map.of(
                        "name", "Maria Silva",
                        "code", "maria.silva",
                        "permissionLevel", "MANAGER"
                ))
                .when()
                .post("/api/operators");
    }

    @Quando("ele listar operadores sem informar a filial ativa")
    public void whenListingOperatorsWithoutCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/operators",
                Map.of("Cookie", "kalles_auth_token=" + authContext.authCookie())
        );
    }

    @Quando("ele consultar o operador externo no contexto da filial ativa")
    public void whenFetchingForeignOperatorFromActiveCompany() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/operators/" + foreignOperatorId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Entao("a resposta de operator deve ter status HTTP {int}")
    public void thenOperatorResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o operador criado deve retornar o codigo {string}")
    public void thenCreatedOperatorShouldReturnCode(String code) {
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para operators")
    public void thenOperatorShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }
}
