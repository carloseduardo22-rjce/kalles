package dev.kalles.sale.cashregister.integration;

import dev.kalles.sale.cashregister.support.AbstractCashRegisterApiSupport;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
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
}
