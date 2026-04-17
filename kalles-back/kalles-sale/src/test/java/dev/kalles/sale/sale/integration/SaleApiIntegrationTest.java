package dev.kalles.sale.sale.integration;

import dev.kalles.sale.sale.support.AbstractSaleApiSupport;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class SaleApiIntegrationTest extends AbstractSaleApiSupport {

    @BeforeEach
    void setUp() {
        prepareSaleScenario(true);
    }

    @Test
    void shouldCreateSaleApplyDiscountReceiveCashAndCompleteSale() {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        givenAuthenticated(auth, sessionToken)
                .when()
                .post("/api/sales/{sessionToken}")
                .then()
                .statusCode(201)
                .body("state", equalTo("OPEN"))
                .body("items", hasSize(0));

        Response addItemResponse = givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "INTERNAL_CODE",
                        "code", PRODUCT_INTERNAL_CODE,
                        "quantity", 1
                ))
                .when()
                .post("/api/sales/{sessionToken}/items");

        addItemResponse.then()
                .statusCode(200)
                .body("items", hasSize(1))
                .body("subtotal", equalTo(30.0F))
                .body("total", equalTo(30.0F));

        String itemId = addItemResponse.jsonPath().getString("items[0].id");
        assertNotNull(itemId);

        patchJson(
                auth,
                "/api/sales/" + sessionToken + "/items/discount",
                "{\"itemId\":\"" + itemId + "\",\"discountAmount\":5.00}"
        )
                .then()
                .statusCode(204);

        getJson(auth, "/api/sales/" + sessionToken)
                .then()
                .statusCode(200)
                .body("total", equalTo(25.0F))
                .body("items[0].discount", equalTo(5.0F));

        givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "method", "CASH",
                        "amount", 30.00
                ))
                .when()
                .post("/api/sales/{sessionToken}/payments")
                .then()
                .statusCode(200)
                .body("state", equalTo("PAID"))
                .body("amountDue", equalTo(0.0F))
                .body("payments", hasSize(1))
                .body("payments[0].method", equalTo("CASH"))
                .body("payments[0].changeAmount", equalTo(5.0F));

        givenAuthenticated(auth, sessionToken)
                .when()
                .post("/api/sales/{sessionToken}/complete")
                .then()
                .statusCode(204);

        getJson(auth, "/api/sales/" + sessionToken)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldRejectDiscountGreaterThanItemTotal() {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        givenAuthenticated(auth, sessionToken)
                .when()
                .post("/api/sales/{sessionToken}")
                .then()
                .statusCode(201);

        Response addItemResponse = givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "INTERNAL_CODE",
                        "code", PRODUCT_INTERNAL_CODE,
                        "quantity", 1
                ))
                .when()
                .post("/api/sales/{sessionToken}/items");

        addItemResponse.then().statusCode(200);
        String itemId = addItemResponse.jsonPath().getString("items[0].id");
        assertNotNull(itemId);

        patchJson(
                auth,
                "/api/sales/" + sessionToken + "/items/discount",
                "{\"itemId\":\"" + itemId + "\",\"discountAmount\":35.00}"
        )
                .then()
                .statusCode(400)
                .body("detail", equalTo("O desconto não pode exceder o valor do produto. Valor do item: R$ 30.00"));
    }

    @Test
    void shouldRejectPixPaymentWhenSessionWasOpenedInCashOnlyMode() {
        prepareSaleScenario(false);
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, true);

        givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "INTERNAL_CODE",
                        "code", PRODUCT_INTERNAL_CODE,
                        "quantity", 1
                ))
                .when()
                .post("/api/sales/{sessionToken}/items")
                .then()
                .statusCode(200);

        givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "method", "PIX",
                        "amount", 30.00
                ))
                .when()
                .post("/api/sales/{sessionToken}/payments")
                .then()
                .statusCode(409)
                .body("detail", equalTo("Esta sessao foi aberta em modo somente dinheiro. PIX, vouchers e cartoes estao indisponiveis."));
    }

    @Test
    void shouldRequireSupervisorAuthorizationToCancelSaleWhenOperatorLacksPermission() {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        givenAuthenticated(auth, sessionToken)
                .when()
                .post("/api/sales/{sessionToken}")
                .then()
                .statusCode(201);

        givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "INTERNAL_CODE",
                        "code", PRODUCT_INTERNAL_CODE,
                        "quantity", 1
                ))
                .when()
                .post("/api/sales/{sessionToken}/items")
                .then()
                .statusCode(200);

        deleteWithHeaders(
                auth,
                "/api/sales/" + sessionToken,
                Map.of("X-Operator-Id", basicOperatorId.toString())
        )
                .then()
                .statusCode(403)
                .body("detail", equalTo("Operador não possui permissão para cancelar vendas. Solicite autorização de um supervisor."));

        deleteWithHeaders(
                auth,
                "/api/sales/" + sessionToken,
                Map.of(
                        "X-Operator-Id", basicOperatorId.toString(),
                        "X-Authorizer-Id", supervisorAuthorizerId.toString()
                )
        )
                .then()
                .statusCode(204);

        getJson(auth, "/api/sales/" + sessionToken)
                .then()
                .statusCode(404);
    }

    private io.restassured.specification.RequestSpecification givenAuthenticated(AuthContext auth, String sessionToken) {
        return given()
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .pathParam("sessionToken", sessionToken);
    }
}
