package dev.kalles.sale.client.steps;

import dev.kalles.sale.payment.support.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientSteps extends ClientCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;
    private UUID foreignClientId;

    @Before
    public void beforeScenario() {
        resetClientScenario();
        authContext = null;
        response = null;
        foreignClientId = null;
    }

    @Dado("um admin autenticado para clients")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
    }

    @Dado("um cliente cadastrado em outra filial")
    public void givenClientInAnotherCompany() {
        foreignClientId = seedClient(companyBId, "Bruna", "28625587887").getId();
    }

    @Quando("ele cadastrar um cliente na filial ativa")
    public void whenCreatingClientInActiveCompany() {
        response = givenAuthenticated(authContext, companyAId)
                .body(Map.of(
                        "name", "Maria Souza",
                        "birthDate", "1990-05-20",
                        "gender", "F",
                        "cpf", "52998224725",
                        "codeCountry", "+55",
                        "cellphone", "11999999999"
                ))
                .when()
                .post("/api/clients");
    }

    @Quando("ele listar clientes sem informar a filial ativa")
    public void whenListingClientsWithoutCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients",
                Map.of("Cookie", "kalles_auth_token=" + authContext.authCookie())
        );
    }

    @Quando("ele consultar o cliente externo no contexto da filial ativa")
    public void whenFetchingForeignClientFromActiveCompany() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients/" + foreignClientId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Entao("a resposta de client deve ter status HTTP {int}")
    public void thenClientResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o cliente criado deve retornar o CPF {string}")
    public void thenCreatedClientShouldReturnCpf(String cpf) {
        assertThat(response.jsonPath().getString("cpf")).isEqualTo(cpf);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para clients")
    public void thenClientShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }
}
