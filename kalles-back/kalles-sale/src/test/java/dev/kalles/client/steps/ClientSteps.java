package dev.kalles.client.steps;

import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientSteps extends ClientCucumberSpringConfiguration {

    private AuthContext authContext;
    private Response response;
    private UUID foreignClientId;
    private UUID registeredClientId;

    @Before
    public void beforeScenario() {
        resetClientScenario();
        authContext = null;
        response = null;
        foreignClientId = null;
        registeredClientId = null;
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
                        "personType", "FISICA",
                        "document", "52998224725",
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

    @Dado("uma pessoa juridica cadastrada na filial ativa com CNPJ {string}")
    public void givenLegalEntityInActiveCompany(String cnpj) {
        registeredClientId = createClient(companyAId, legalEntity("Empresa Cadastrada Ltda", cnpj));
    }

    @Dado("uma pessoa juridica cadastrada em outra filial com CNPJ {string}")
    public void givenLegalEntityInAnotherCompany(String cnpj) {
        registeredClientId = createClient(companyBId, legalEntity("Empresa Cadastrada Ltda", cnpj));
    }

    @Dado("uma pessoa fisica cadastrada na filial ativa com CPF {string}")
    public void givenNaturalPersonInActiveCompany(String cpf) {
        registeredClientId = createClient(companyAId, naturalPerson("Maria Souza", cpf));
    }

    @Dado("um cliente gravado sem tipo na filial ativa com documento {string}")
    public void givenClientStoredWithoutType(String document) {
        registeredClientId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO client (id, company_id, name, document) VALUES (?, ?, ?, ?)",
                registeredClientId, companyAId, "Cliente Antigo", document
        );
    }

    @Dado("uma politica de fidelidade ativa na filial ativa")
    public void givenActiveFidelityPolicy() {
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(companyAId);
        policy.setObjectivePoints(100);
        policy.setConfiguredDiscount(new BigDecimal("20.00"));
        policy.setValuePoint(1);
        policy.setActive(true);
        fidelityPolicyRepository.save(policy);
    }

    @Quando("ele cadastrar uma pessoa juridica {string} com CNPJ {string}")
    public void whenCreatingLegalEntity(String corporateName, String cnpj) {
        response = postClient(companyAId, legalEntity(corporateName, cnpj));
    }

    @Quando("ele cadastrar uma pessoa juridica com CNPJ {string} informando {string} igual a {string}")
    public void whenCreatingLegalEntityWithNaturalPersonField(String cnpj, String field, String value) {
        Map<String, Object> body = legalEntity("Empresa Teste Ltda", cnpj);
        body.put(field, value);
        response = postClient(companyAId, body);
    }

    @Quando("ele cadastrar uma pessoa fisica {string} com CPF {string}")
    public void whenCreatingNaturalPerson(String name, String cpf) {
        response = postClient(companyAId, naturalPerson(name, cpf));
    }

    @Quando("ele alterar esse cliente para pessoa juridica com CNPJ {string}")
    public void whenChangingClientToLegalEntity(String cnpj) {
        response = LocalHttpTestClient.put(
                "http://localhost:" + port + "/api/clients/" + registeredClientId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie() + "; XSRF-TOKEN=" + authContext.csrfCookie(),
                        "X-XSRF-TOKEN", authContext.csrfToken(),
                        "X-Company-ID", companyAId.toString(),
                        "Content-Type", "application/json"
                ),
                """
                {"personType": "JURIDICA", "name": "Maria Souza Ltda", "document": "%s"}
                """.formatted(cnpj)
        );
    }

    @Quando("ele consultar esse cliente")
    public void whenFetchingRegisteredClient() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/clients/" + registeredClientId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authContext.authCookie(),
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Quando("ele inscrever esse cliente no programa de fidelidade")
    public void whenEnrollingRegisteredClientInFidelity() {
        response = givenAuthenticated(authContext, companyAId)
                .when()
                .post("/api/fidelity/enroll/" + registeredClientId);
    }

    @Entao("o cliente criado deve ter tipo {string} e documento {string}")
    public void thenCreatedClientShouldHaveTypeAndDocument(String personType, String document) {
        assertThat(response.jsonPath().getString("personType")).isEqualTo(personType);
        assertThat(response.jsonPath().getString("document")).isEqualTo(document);
    }

    @Entao("o cliente consultado deve ter tipo {string}")
    public void thenFetchedClientShouldHaveType(String personType) {
        assertThat(response.jsonPath().getString("personType")).isEqualTo(personType);
    }

    @Entao("o detalhe do erro de client deve conter {string}")
    public void thenClientErrorDetailShouldContain(String fragment) {
        assertThat(response.jsonPath().getString("detail")).contains(fragment);
    }

    @Entao("o codigo de erro de client deve ser {string}")
    public void thenClientErrorCodeShouldBe(String code) {
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    @Entao("a resposta de client deve ter status HTTP {int}")
    public void thenClientResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o cliente criado deve retornar o CPF {string}")
    public void thenCreatedClientShouldReturnCpf(String cpf) {
        assertThat(response.jsonPath().getString("document")).isEqualTo(cpf);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para clients")
    public void thenClientShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }

    private Map<String, Object> legalEntity(String corporateName, String cnpj) {
        Map<String, Object> body = new HashMap<>();
        body.put("personType", "JURIDICA");
        body.put("name", corporateName);
        body.put("document", cnpj);
        return body;
    }

    private Map<String, Object> naturalPerson(String name, String cpf) {
        Map<String, Object> body = new HashMap<>();
        body.put("personType", "FISICA");
        body.put("name", name);
        body.put("document", cpf);
        body.put("birthDate", "1990-05-20");
        body.put("gender", "F");
        return body;
    }

    private Response postClient(UUID companyId, Map<String, Object> body) {
        return givenAuthenticated(authContext, companyId)
                .body(body)
                .when()
                .post("/api/clients");
    }

    private UUID createClient(UUID companyId, Map<String, Object> body) {
        Response created = postClient(companyId, body);
        assertThat(created.statusCode()).isEqualTo(201);
        return UUID.fromString(created.jsonPath().getString("id"));
    }
}
