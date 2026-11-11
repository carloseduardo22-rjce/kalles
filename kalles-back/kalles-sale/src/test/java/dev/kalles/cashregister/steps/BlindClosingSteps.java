package dev.kalles.cashregister.steps;

import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class BlindClosingSteps extends CashRegisterCucumberSpringConfiguration {

    private AuthContext operatorAuth;
    private AuthContext adminAuth;
    private UUID sessionId;
    private UUID foreignCompanyId;
    private Response response;

    @Before(value = "@fechamentoCego", order = 20000)
    public void beforeScenario() {
        resetScenarioData();
        operatorAuth = null;
        adminAuth = null;
        sessionId = null;
        foreignCompanyId = null;
        response = null;
    }

    @Dado("um operador com sessao aberta e fundo de troco de {string}")
    public void givenOperatorWithOpenSession(String initialAmount) {
        operatorAuth = authenticateOperator();
        sessionId = seedSession(companyId, CASH_REGISTER_CODE, OPERATOR_CODE, new BigDecimal(initialAmount));
    }

    @Dado("um administrador autenticado na loja")
    public void givenAdministrator() {
        adminAuth = authenticateAdmin();
    }

    @Dado("a tolerancia de caixa da loja de {string}")
    public void givenStoreTolerance(String amount) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        company.setCashClosingTolerance(new BigDecimal(amount));
        companyRepository.save(company);
    }

    @Dado("um supervisor cadastrado com matricula {string}")
    public void givenSupervisor(String code) {
        seedOperator(companyId, "Supervisor da loja", code, PermissionLevel.SUPERVISOR);
    }

    @Dado("um operador de nivel basico cadastrado com matricula {string}")
    public void givenBasicOperator(String code) {
        seedOperator(companyId, "Outro operador", code, PermissionLevel.BASIC);
    }

    @Dado("o operador da sessao tem nivel de supervisor")
    public void givenSessionOperatorIsSupervisor() {
        Operator operator = operatorRepository.findByCodeAndCompanyId(OPERATOR_CODE, companyId).orElseThrow();
        operator.setPermissionLevel(PermissionLevel.SUPERVISOR);
        operatorRepository.save(operator);
    }

    @Quando("o operador informar o valor contado de {string}")
    public void whenOperatorCounts(String counted) {
        close(Map.of("countedCashAmount", new BigDecimal(counted)));
    }

    @Quando("o operador informar o valor contado de {string} com a liberacao de {string}")
    public void whenOperatorCountsWithAuthorization(String counted, String authorizerCode) {
        close(Map.of("countedCashAmount", new BigDecimal(counted), "authorizedOperatorCode", authorizerCode));
    }

    @Quando("o operador consultar os detalhes com a sessao aberta")
    public void whenOperatorReadsDetails() {
        response = get(operatorAuth, "/api/cash-register-sessions/" + sessionId);
    }

    @Quando("o operador consultar os detalhes da sessao fechada")
    public void whenOperatorReadsClosedSessionDetails() {
        assertThat(session().isOpen()).isFalse();
        response = get(operatorAuth, "/api/cash-register-sessions/" + sessionId);
    }

    @Quando("o operador consultar a tolerancia da loja")
    public void whenOperatorReadsTolerance() {
        response = get(operatorAuth, "/api/companies/" + companyId + "/cash-closing-tolerance");
    }

    @Dado("uma loja de outro tenant")
    public void givenStoreFromAnotherTenant() {
        UUID otherTenantId = UUID.randomUUID();
        tenantRepository.save(new Tenant(otherTenantId, "Outro tenant"));
        foreignCompanyId = companyRepository.save(
                new Company(null, "Loja de outro tenant", otherTenantId, null, null, null, null, null, null)
        ).getId();
    }

    @Quando("o administrador definir a tolerancia da loja de outro tenant em {string}")
    public void whenAdministratorSetsForeignTolerance(String amount) {
        response = putTolerance(adminAuth, foreignCompanyId, amount);
    }

    @Quando("o operador consultar o resumo com a sessao aberta")
    public void whenOperatorReadsReport() {
        response = get(operatorAuth, "/api/cash-register-sessions/" + sessionId + "/report");
    }

    @Quando("o operador consultar a lista com a sessao aberta")
    public void whenOperatorListsSessions() {
        LocalDate today = LocalDate.now();
        response = get(operatorAuth, "/api/cash-register-sessions?startDate=" + today + "&endDate=" + today);
    }

    @Quando("o administrador consultar o resumo da sessao aberta")
    public void whenAdministratorReadsReport() {
        response = get(adminAuth, "/api/cash-register-sessions/" + sessionId + "/report");
    }

    @Quando("o administrador definir a tolerancia da loja em {string}")
    public void whenAdministratorSetsTolerance(String amount) {
        response = putTolerance(adminAuth, companyId, amount);
    }

    @Quando("o operador definir a tolerancia da loja em {string}")
    public void whenOperatorSetsTolerance(String amount) {
        response = putTolerance(operatorAuth, companyId, amount);
    }

    @Entao("o fechamento deve ter status HTTP {int}")
    public void thenStatus(int status) {
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @Entao("a consulta deve ter status HTTP {int}")
    public void thenQueryStatus(int status) {
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @Entao("a configuracao deve ter status HTTP {int}")
    public void thenSettingStatus(int status) {
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @E("a tolerancia da loja de outro tenant deve continuar {string}")
    public void thenForeignToleranceUnchanged(String amount) {
        assertThat(companyRepository.findById(foreignCompanyId).orElseThrow().getCashClosingTolerance())
                .isEqualByComparingTo(amount);
    }

    @Entao("o erro do fechamento deve ter o codigo {string}")
    public void thenErrorCode(String code) {
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    @Entao("o saldo esperado deve ser {string}")
    public void thenExpectedCash(String amount) {
        assertThat(new BigDecimal(response.jsonPath().getString("saldoEsperadoEmCaixa")))
                .isEqualByComparingTo(amount);
    }

    @Entao("a diferenca registrada deve ser {string}")
    public void thenDifference(String amount) {
        assertThat(new BigDecimal(response.jsonPath().getString("resumo.diferencaEmCaixa")))
                .isEqualByComparingTo(amount);
    }

    @Entao("o fechamento nao deve ter autorizador")
    public void thenNoAuthorizer() {
        assertThat(response.jsonPath().getString("codigoOperadorAutorizador")).isNull();
    }

    @Entao("o fechamento deve ter sido liberado por {string}")
    public void thenAuthorizedBy(String code) {
        assertThat(response.jsonPath().getString("codigoOperadorAutorizador")).isEqualTo(code);
    }

    @Entao("a resposta nao deve revelar o valor esperado")
    public void thenExpectedCashIsNotRevealed() {
        Map<String, Object> body = response.jsonPath().getMap("$");
        assertThat(body).doesNotContainKeys("resumo", "saldoEsperadoEmCaixa", "diferencaEmCaixa");
        assertThat(response.asString()).doesNotContain("saldo", "diferenca", "100.0", "100,0", "5.01", "5,01");
    }

    @Entao("a sessao deve estar fechada")
    public void thenSessionIsClosed() {
        assertThat(session().isOpen()).isFalse();
    }

    @Entao("a sessao deve continuar aberta")
    public void thenSessionStaysOpen() {
        assertThat(session().isOpen()).isTrue();
    }

    @E("a primeira contagem divergente deve ser {string}")
    public void thenFirstDivergentCount(String amount) {
        assertThat(session().getFirstDivergentCountedAmount()).isEqualByComparingTo(amount);
    }

    @E("a tolerancia da loja deve ser {string}")
    public void thenStoreTolerance(String amount) {
        Response tolerance = get(adminAuth, "/api/companies/" + companyId + "/cash-closing-tolerance");
        assertThat(tolerance.statusCode()).isEqualTo(200);
        assertThat(new BigDecimal(tolerance.jsonPath().getString("amount"))).isEqualByComparingTo(amount);
    }

    private CashRegisterSession session() {
        return cashRegisterSessionRepository.findById(sessionId).orElseThrow();
    }

    private void close(Map<String, Object> body) {
        response = authenticated(operatorAuth)
                .header("X-Company-ID", companyId.toString())
                .body(new HashMap<>(body))
                .when()
                .post("/api/cash-register-sessions/{sessionId}/close", sessionId);
    }

    private Response putTolerance(AuthContext auth, UUID targetCompanyId, String amount) {
        return LocalHttpTestClient.put(
                "http://localhost:" + port + "/api/companies/" + targetCompanyId + "/cash-closing-tolerance",
                Map.of(
                        "Content-Type", "application/json",
                        "Cookie", "kalles_auth_token=" + auth.authCookie() + "; XSRF-TOKEN=" + auth.csrfCookie(),
                        "X-XSRF-TOKEN", auth.csrfToken()
                ),
                "{\"amount\": " + amount + "}");
    }

    private Response get(AuthContext auth, String path) {
        return LocalHttpTestClient.get(
                "http://localhost:" + port + path,
                Map.of("Cookie", "kalles_auth_token=" + auth.authCookie(), "X-Company-ID", companyId.toString()));
    }

    private RequestSpecification authenticated(AuthContext auth) {
        return given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken());
    }
}
