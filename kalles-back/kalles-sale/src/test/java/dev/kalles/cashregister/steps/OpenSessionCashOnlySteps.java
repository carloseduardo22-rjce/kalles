package dev.kalles.cashregister.steps;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenSessionCashOnlySteps extends CashRegisterCucumberSpringConfiguration {

    private boolean paymentIntegrationConfigured;
    private boolean allowCashOnlyOperation;
    private BigDecimal initialAmount;
    private AuthContext authContext;
    private Response response;

    @Before
    public void beforeScenario() {
        resetScenarioData();
        paymentIntegrationConfigured = false;
        allowCashOnlyOperation = false;
        initialAmount = BigDecimal.ZERO;
        response = null;
    }

    @Dado("que o caixa possui integracao de pagamento configurada")
    public void givenCashRegisterHasPaymentIntegrationConfigured() {
        paymentIntegrationConfigured = true;
        configurePaymentIntegration(true);
    }

    @Dado("que o caixa nao possui integracao de pagamento configurada")
    public void givenCashRegisterHasNoPaymentIntegrationConfigured() {
        paymentIntegrationConfigured = false;
        configurePaymentIntegration(false);
    }

    @Dado("um operador autenticado deseja abrir a sessao do caixa com valor inicial {string}")
    public void givenAuthenticatedOperatorWantsToOpenSession(String rawInitialAmount) {
        initialAmount = new BigDecimal(rawInitialAmount);
        authContext = authenticateOperator();
    }

    @Dado("o operador confirma a abertura em modo somente dinheiro")
    public void givenOperatorConfirmsCashOnlyOperation() {
        allowCashOnlyOperation = true;
    }

    @Quando("solicitar a abertura da sessao")
    public void whenRequestSessionOpening() {
        Map<String, Object> body = new HashMap<>();
        body.put("cashRegisterCode", CASH_REGISTER_CODE);
        body.put("operatorCode", OPERATOR_CODE);
        body.put("initialAmount", initialAmount);
        body.put("allowCashOnlyOperation", allowCashOnlyOperation);

        response = given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(body)
                .when()
                .post("/api/cash-register-sessions/open");
    }

    @Entao("a resposta da abertura deve ter status HTTP {int}")
    public void thenOpenSessionResponseShouldHaveStatusCode(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a sessao retornada nao deve estar em modo somente dinheiro")
    public void thenReturnedSessionShouldNotBeCashOnly() {
        assertThat(paymentIntegrationConfigured).isTrue();
        assertThat(response.jsonPath().getBoolean("cashOnlyOperation")).isFalse();
    }

    @Entao("a sessao retornada deve estar em modo somente dinheiro")
    public void thenReturnedSessionShouldBeCashOnly() {
        assertThat(response.jsonPath().getBoolean("cashOnlyOperation")).isTrue();
    }

    @Entao("a resposta deve informar {string}")
    public void thenResponseShouldContainMessage(String message) {
        assertThat(response.jsonPath().getString("detail")).isEqualTo(message);
    }
}
