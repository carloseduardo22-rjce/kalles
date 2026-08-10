package dev.kalles.security.companycontext;

import dev.kalles.testsupport.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class CompanyContextSteps extends CompanyContextCucumberSpringConfiguration {

    private String authCookie;
    private Response response;

    @Before
    public void beforeScenario() {
        resetScenarioData();
        authCookie = null;
        response = null;
    }

    @Dado("um admin autenticado sem filial fixa no token")
    public void givenTenantAdminWithoutFixedCompany() {
        authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
    }

    @Dado("um admin autenticado com filial fixa no token")
    public void givenBoundAdminWithFixedCompany() {
        authCookie = loginAndExtractAuthCookie(BOUND_ADMIN_EMAIL);
    }

    @Quando("ele consultar os caixas sem informar a filial ativa")
    public void whenHeRequestsRegistersWithoutHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );
    }

    @Quando("ele consultar os caixas informando uma filial acessivel do proprio tenant")
    public void whenHeRequestsRegistersWithAccessibleCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", companyBId.toString()
                )
        );
    }

    @Quando("ele consultar os caixas informando outra filial no header")
    public void whenHeRequestsRegistersWithConflictingHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-registers",
                java.util.Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", companyBId.toString()
                )
        );
    }

    @Entao("a resposta de contexto de filial deve ter status HTTP {int}")
    public void thenResponseShouldHaveCompanyContextStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria")
    public void thenResponseShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }

    @Entao("a lista de caixas deve conter apenas os caixas da filial informada")
    public void thenRegisterListShouldContainOnlySelectedCompanyRegisters() {
        assertThat(response.jsonPath().getList("$")).hasSize(1);
        assertThat(response.jsonPath().getString("[0].code")).isEqualTo("CX-B1");
    }

    @Entao("a resposta deve informar que o contexto de filial foi negado")
    public void thenResponseShouldInformCompanyContextWasDenied() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_DENIED");
    }
}
