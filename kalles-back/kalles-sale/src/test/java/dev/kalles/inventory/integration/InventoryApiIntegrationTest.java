package dev.kalles.inventory.integration;

import dev.kalles.inventory.support.AbstractInventoryApiSupport;
import dev.kalles.testsupport.LocalHttpTestClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

@Tag("integration")
class InventoryApiIntegrationTest extends AbstractInventoryApiSupport {

    @BeforeEach
    void setUp() {
        resetInventoryScenario();
    }

    @Test
    void shouldRequireCompanyHeaderForWarehouseListing() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/warehouses",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );

        response.then()
                .statusCode(400)
                .body("code", equalTo("COMPANY_CONTEXT_REQUIRED"));
    }

    @Test
    void shouldCreateProductWithinCurrentTenantAndCompany() {
        AuthContext auth = authenticateTenantAdminWithCsrf();

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "name", "Arroz Tipo 1",
                        "internalCode", "ARZ-001",
                        "barcode", "789100000001",
                        "description", "Pacote 5kg",
                        "price", 32.90,
                        "costPrice", 24.50
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Arroz Tipo 1"))
                .body("internalCode", equalTo("ARZ-001"))
                .body("stockQuantity", equalTo(0));
    }

    @Test
    void shouldReturnNotFoundWhenFetchingWarehouseFromAnotherCompany() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        UUID warehouseId = seedWarehouse(companyBId, "Deposito B1");

        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/warehouses/" + warehouseId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", companyAId.toString()
                )
        );

        response.then().statusCode(404);
    }

    @Test
    void shouldReturnNotFoundWhenSettingStockForProductFromAnotherTenant() {
        AuthContext auth = authenticateTenantAdminWithCsrf();
        UUID foreignProductId = seedProduct(OTHER_TENANT_ID, foreignCompanyId, "EXT-001", "789200000001");
        UUID locationId = seedLocation(companyAId, "A-01");

        givenAuthenticated(auth, companyAId)
                .body(Map.of(
                        "productId", foreignProductId,
                        "locationId", locationId,
                        "quantity", 10,
                        "unitCost", 5.00
                ))
                .when()
                .post("/api/stocks")
                .then()
                .statusCode(404);
    }
}
