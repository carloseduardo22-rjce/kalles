package dev.kalles.fidelity.steps;

import dev.kalles.fidelity.enums.FidelityDiscountType;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FidelityPolicySteps extends FidelityPolicyCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;

    @Before
    public void beforeScenario() {
        resetFidelityPolicyScenario();
        authContext = null;
        response = null;
    }

    @Dado("um admin autenticado para fidelity policy")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
    }

    @Dado("uma politica ativa cadastrada em outra filial")
    public void givenPolicyInAnotherCompany() {
        seedPolicy(companyBId, 300, "30.00", 3, FidelityDiscountType.FIXED, true, LocalDate.now());
    }

    @Quando("ele cadastrar uma politica de fidelidade na filial ativa")
    public void whenCreatingPolicyInActiveCompany() {
        response = givenAuthenticated(authContext, companyAId)
                .body(Map.of(
                        "objectivePoints", 200,
                        "configuredDiscount", 15.00,
                        "valuePoint", 2,
                        "discountType", "PERCENTAGE"
                ))
                .when()
                .post("/api/fidelity-policies");
    }

    @Quando("ele consultar a politica ativa sem informar a filial ativa")
    public void whenFetchingActivePolicyWithoutCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/fidelity-policies/active",
                Map.of("Cookie", "kalles_auth_token=" + authContext.authCookie())
        );
    }

    @Quando("ele listar as politicas no contexto da filial ativa")
    public void whenListingPoliciesFromActiveCompany() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/fidelity-policies",
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Entao("a resposta de fidelity policy deve ter status HTTP {int}")
    public void thenFidelityPolicyResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a politica criada deve retornar o objetivo de pontos {int}")
    public void thenCreatedPolicyShouldReturnObjectivePoints(int objectivePoints) {
        assertThat(response.jsonPath().getInt("objectivePoints")).isEqualTo(objectivePoints);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para fidelity policy")
    public void thenFidelityPolicyShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }

    @Entao("a listagem nao deve incluir a politica da outra filial")
    public void thenPolicyListingShouldNotIncludeForeignCompanyPolicy() {
        assertThat(response.jsonPath().getList("$")).hasSize(0);
    }
}
