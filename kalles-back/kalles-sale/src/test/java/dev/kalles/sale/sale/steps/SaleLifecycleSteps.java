package dev.kalles.sale.sale.steps;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class SaleLifecycleSteps extends SaleCucumberSpringConfiguration {

    private boolean cashOnlyOperation;
    private AuthContext authContext;
    private String sessionToken;
    private Response lastResponse;
    private String lastItemId;

    @Before
    public void beforeScenario() {
        cashOnlyOperation = false;
        authContext = null;
        sessionToken = null;
        lastResponse = null;
        lastItemId = null;
    }

    @Dado("que existe um produto disponivel para venda no PDV")
    public void givenProductAvailableForSale() {
        prepareSaleScenario(true);
    }

    @Dado("um operador autenticado com sessao de caixa aberta em modo normal")
    public void givenOperatorAuthenticatedWithNormalSession() {
        prepareAuthenticatedSession(false);
    }

    @Dado("um operador autenticado com sessao de caixa aberta em modo somente dinheiro")
    public void givenOperatorAuthenticatedWithCashOnlySession() {
        prepareSaleScenario(false);
        prepareAuthenticatedSession(true);
    }

    @Quando("iniciar uma nova venda no PDV")
    public void whenStartNewSale() {
        startNewSale();
    }

    private void startNewSale() {
        lastResponse = request()
                .when()
                .post("/api/sales/{sessionToken}");
    }

    @Quando("adicionar um item pelo codigo interno {string} com quantidade {int}")
    public void whenAddItemByInternalCode(String internalCode, int quantity) {
        addItemByInternalCode(internalCode, quantity);
    }

    private void addItemByInternalCode(String internalCode, int quantity) {
        lastResponse = request()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "INTERNAL_CODE",
                        "code", internalCode,
                        "quantity", quantity
                ))
                .when()
                .post("/api/sales/{sessionToken}/items");

        if (lastResponse.statusCode() == 200) {
            lastItemId = lastResponse.jsonPath().getString("items[0].id");
        }
    }

    @Quando("aplicar um desconto de {string} no item da venda")
    public void whenApplyDiscount(String rawDiscount) {
        lastResponse = patchJson(
                authContext,
                "/api/sales/" + sessionToken + "/items/discount",
                "{\"itemId\":\"" + lastItemId + "\",\"discountAmount\":" + rawDiscount + "}",
                Map.of("X-Operator-Id", supervisorAuthorizerId.toString())
        );
    }

    @Quando("registrar um pagamento em dinheiro de {string}")
    public void whenRegisterCashPayment(String rawAmount) {
        whenRegisterPayment("CASH", rawAmount);
    }

    @Quando("registrar um pagamento via {string} no valor de {string}")
    public void whenRegisterPayment(String method, String rawAmount) {
        lastResponse = request()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "method", method,
                        "amount", new BigDecimal(rawAmount)
                ))
                .when()
                .post("/api/sales/{sessionToken}/payments");
    }

    @Quando("concluir a venda atual")
    public void whenCompleteCurrentSale() {
        lastResponse = request()
                .when()
                .post("/api/sales/{sessionToken}/complete");
    }

    @Quando("solicitar o cancelamento da venda com operador basico")
    public void whenCancelSaleWithBasicOperator() {
        lastResponse = deleteWithHeaders(
                authContext,
                "/api/sales/" + sessionToken,
                Map.of("X-Operator-Id", basicOperatorId.toString())
        );
    }

    @Quando("solicitar o cancelamento da venda com operador basico autorizado por supervisor")
    public void whenCancelSaleWithSupervisorAuthorization() {
        lastResponse = deleteWithHeaders(
                authContext,
                "/api/sales/" + sessionToken,
                Map.of(
                        "X-Operator-Id", basicOperatorId.toString(),
                        "X-Authorizer-Id", supervisorAuthorizerId.toString()
                )
        );
    }

    @Entao("a operacao da venda deve responder com status HTTP {int}")
    public void thenOperationShouldReturnStatusCode(int statusCode) {
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta da venda deve informar {string}")
    public void thenSaleResponseShouldContainMessage(String message) {
        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.jsonPath().getString("detail")).isEqualTo(message);
    }

    private void prepareAuthenticatedSession(boolean cashOnly) {
        cashOnlyOperation = cashOnly;
        authContext = authenticateOperator();
        sessionToken = openSession(authContext, cashOnlyOperation);
    }

    private io.restassured.specification.RequestSpecification request() {
        return given()
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .pathParam("sessionToken", sessionToken);
    }
}
