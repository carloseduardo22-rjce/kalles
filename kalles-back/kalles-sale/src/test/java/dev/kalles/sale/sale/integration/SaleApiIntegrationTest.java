package dev.kalles.sale.sale.integration;

import dev.kalles.sale.sale.support.AbstractSaleApiSupport;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class SaleApiIntegrationTest extends AbstractSaleApiSupport {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
                "{\"itemId\":\"" + itemId + "\",\"discountAmount\":5.00}",
                Map.of("X-Operator-Id", supervisorAuthorizerId.toString())
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
                "{\"itemId\":\"" + itemId + "\",\"discountAmount\":35.00}",
                Map.of("X-Operator-Id", supervisorAuthorizerId.toString())
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

    @Test
    void shouldListSaleHistoryForActiveCompany() {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        completeSimpleSale(auth, sessionToken);

        LocalDate today = LocalDate.now();
        getJson(auth, "/api/sales/history?startDate=" + today + "&endDate=" + today)
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].sessionToken", equalTo(sessionToken))
                .body("[0].companyId", equalTo(companyId.toString()))
                .body("[0].state", equalTo("COMPLETED"))
                .body("[0].items", hasSize(1))
                .body("[0].payments", hasSize(1));
    }

    @Test
    void shouldFilterSaleHistoryByState() {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        completeSimpleSale(auth, sessionToken);

        LocalDate today = LocalDate.now();
        getJson(auth, "/api/sales/history?startDate=" + today + "&endDate=" + today + "&state=CANCELED")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void shouldRejectSaleHistoryWhenPeriodIsInvalid() {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        getJson(auth, "/api/sales/history?startDate=2026-04-30&endDate=2026-04-01")
                .then()
                .statusCode(400)
                .body("detail", equalTo("A data final nao pode ser menor que a data inicial."));
    }

    @Test
    void shouldExportSaleHistoryToExcel() throws IOException {
        AuthContext auth = authenticateOperator();
        String sessionToken = openSession(auth, false);

        completeSimpleSale(auth, sessionToken);

        LocalDate today = LocalDate.now();
        RawHttpResponse response = getBytes(auth, "/api/sales/history/export?startDate=" + today + "&endDate=" + today);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.firstHeader("content-type"))
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(response.firstHeader("content-disposition"))
                .isEqualTo("attachment; filename=\"historico-vendas.xlsx\"");
        assertThat(response.body()).isNotEmpty();

        Map<String, String> entries = unzip(response.body());
        assertThat(entries.get("xl/workbook.xml")).contains("sales", "sale_items", "payments");
        assertThat(entries.get("xl/worksheets/sheet1.xml"))
                .contains("session_token", "company_id", "fidelity_discount_applied", sessionToken);
        assertThat(entries.get("xl/worksheets/sheet2.xml"))
                .contains("sale_id", "product_id", "unit_price");
        assertThat(entries.get("xl/worksheets/sheet3.xml"))
                .contains("sale_id", "method", "change_amount");
    }

    private io.restassured.specification.RequestSpecification givenAuthenticated(AuthContext auth, String sessionToken) {
        return given()
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .pathParam("sessionToken", sessionToken);
    }

    private io.restassured.specification.RequestSpecification givenAuthenticated(AuthContext auth) {
        return given()
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString());
    }

    private void completeSimpleSale(AuthContext auth, String sessionToken) {
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

        givenAuthenticated(auth, sessionToken)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "method", "CASH",
                        "amount", 30.00
                ))
                .when()
                .post("/api/sales/{sessionToken}/payments")
                .then()
                .statusCode(200);

        givenAuthenticated(auth, sessionToken)
                .when()
                .post("/api/sales/{sessionToken}/complete")
                .then()
                .statusCode(204);
    }

    private Map<String, String> unzip(byte[] content) throws IOException {
        Map<String, String> entries = new java.util.LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }

    private RawHttpResponse getBytes(AuthContext auth, String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header(
                            "Cookie",
                            "kalles_auth_token=" + auth.authCookie()
                                    + "; XSRF-TOKEN=" + auth.csrfCookie()
                    )
                    .header("X-XSRF-TOKEN", auth.csrfToken())
                    .header("X-Company-ID", companyId.toString())
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return new RawHttpResponse(response.statusCode(), response.headers().map(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fail to execute sale export request for tests", e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to execute sale export request for tests", e);
        }
    }

    private record RawHttpResponse(int statusCode, Map<String, List<String>> headers, byte[] body) {
        String firstHeader(String name) {
            return headers.getOrDefault(name, List.of()).stream().findFirst().orElse(null);
        }
    }
}
