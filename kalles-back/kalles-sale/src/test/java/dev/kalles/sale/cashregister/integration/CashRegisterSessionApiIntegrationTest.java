package dev.kalles.sale.cashregister.integration;

import dev.kalles.sale.cashregister.support.AbstractCashRegisterApiSupport;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import dev.kalles.sale.payment.support.LocalHttpTestClient;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;

class CashRegisterSessionApiIntegrationTest extends AbstractCashRegisterApiSupport {

    @BeforeEach
    void setUp() {
        resetScenarioData();
    }

    @Test
    void shouldOpenSessionNormallyWhenPaymentIntegrationIsConfigured() {
        configurePaymentIntegration(true);
        AuthContext auth = authenticateOperator();

        given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "cashRegisterCode", CASH_REGISTER_CODE,
                        "operatorCode", OPERATOR_CODE,
                        "initialAmount", 100.00,
                        "allowCashOnlyOperation", false
                ))
                .when()
                .post("/api/cash-register-sessions/open")
                .then()
                .statusCode(201)
                .body("cashRegisterCode", equalTo(CASH_REGISTER_CODE))
                .body("cashOnlyOperation", equalTo(false))
                .body("status", equalTo("OPEN"));
    }

    @Test
    void shouldRejectOpenSessionWithoutPaymentConfigurationAndWithoutCashOnlyConfirmation() {
        configurePaymentIntegration(false);
        AuthContext auth = authenticateOperator();

        given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "cashRegisterCode", CASH_REGISTER_CODE,
                        "operatorCode", OPERATOR_CODE,
                        "initialAmount", 100.00,
                        "allowCashOnlyOperation", false
                ))
                .when()
                .post("/api/cash-register-sessions/open")
                .then()
                .statusCode(409)
                .body("detail", equalTo("Pagamento nao configurado, neste caixa voce apenas podera operar com dinheiro mas nao podera receber pagamentos via pix, vouchers e cartoes de credito."));
    }

    @Test
    void shouldOpenSessionInCashOnlyModeWhenExplicitlyConfirmed() {
        configurePaymentIntegration(false);
        AuthContext auth = authenticateOperator();

        given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "cashRegisterCode", CASH_REGISTER_CODE,
                        "operatorCode", OPERATOR_CODE,
                        "initialAmount", 100.00,
                        "allowCashOnlyOperation", true
                ))
                .when()
                .post("/api/cash-register-sessions/open")
                .then()
                .statusCode(201)
                .body("cashOnlyOperation", equalTo(true))
                .body("status", equalTo("OPEN"));
    }

    @Test
    void shouldReturnNotFoundWhenFetchingSessionFromAnotherCompany() {
        AuthContext auth = authenticateOperator();
        UUID foreignCompanyId = seedCompany("Loja secundaria");
        seedCashRegister(foreignCompanyId, "CAIXA-02", "Caixa secundario");
        seedOperator(foreignCompanyId, "Operador Secundario", "OP002", null);
        UUID foreignSessionId = seedSession(foreignCompanyId, "CAIXA-02", "OP002", new BigDecimal("50.00"));

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-register-sessions/" + foreignSessionId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyId.toString()
                )
        );

        response.then()
                .statusCode(404);
    }

    @Test
    void shouldRejectClosingSessionWithAuthorizerFromAnotherCompany() {
        AuthContext auth = authenticateOperator();
        UUID sessionId = seedSession(companyId, CASH_REGISTER_CODE, OPERATOR_CODE, new BigDecimal("100.00"));
        UUID foreignCompanyId = seedCompany("Loja secundaria");
        seedOperator(foreignCompanyId, "Supervisor Externo", "SUP999", PermissionLevel.SUPERVISOR);

        given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "authorizedOperatorCode", "SUP999",
                        "countedCashAmount", 100.00
                ))
                .when()
                .post("/api/cash-register-sessions/{sessionId}/close", sessionId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldListOnlySessionsFromActiveCompany() {
        AuthContext auth = authenticateOperator();
        seedSession(companyId, CASH_REGISTER_CODE, OPERATOR_CODE, new BigDecimal("100.00"));

        UUID foreignCompanyId = seedCompany("Loja secundaria");
        seedCashRegister(foreignCompanyId, "CAIXA-02", "Caixa secundario");
        seedOperator(foreignCompanyId, "Operador Secundario", "OP002", null);
        seedSession(foreignCompanyId, "CAIXA-02", "OP002", new BigDecimal("50.00"));

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/cash-register-sessions?startDate=" + java.time.LocalDate.now() + "&endDate=" + java.time.LocalDate.now(),
                Map.of(
                        "Cookie", "kalles_auth_token=" + auth.authCookie(),
                        "X-Company-ID", companyId.toString()
                )
        );

        response.then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].codigoCaixa", equalTo(CASH_REGISTER_CODE));
    }
}
