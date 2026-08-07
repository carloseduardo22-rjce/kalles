package dev.kalles.inventory.steps;

import dev.kalles.payment.support.LocalHttpTestClient;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class InventorySteps extends InventoryCucumberSpringConfiguration {

    private AuthContext authContext;
    private String authCookie;
    private Response response;
    private UUID foreignWarehouseId;
    private UUID foreignProductId;
    private UUID locationId;

    @Before
    public void beforeScenario() {
        resetInventoryScenario();
        authContext = null;
        authCookie = null;
        response = null;
        foreignWarehouseId = null;
        foreignProductId = null;
        locationId = null;
    }

    @Dado("um admin autenticado do tenant atual")
    public void givenAuthenticatedTenantAdmin() {
        authContext = authenticateTenantAdminWithCsrf();
        authCookie = authContext.authCookie();
    }

    @Dado("um deposito cadastrado em outra filial do mesmo tenant")
    public void givenWarehouseInAnotherCompany() {
        foreignWarehouseId = seedWarehouse(companyBId, "Deposito B1");
    }

    @Dado("um produto cadastrado em outro tenant")
    public void givenProductInAnotherTenant() {
        foreignProductId = seedProduct(OTHER_TENANT_ID, foreignCompanyId, "EXT-001", "789200000001");
    }

    @Dado("uma localizacao cadastrada na filial ativa")
    public void givenLocationInActiveCompany() {
        locationId = seedLocation(companyAId, "A-01");
    }

    @Quando("ele listar depositos sem informar a filial ativa")
    public void whenListingWarehousesWithoutCompanyHeader() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/warehouses",
                Map.of("Cookie", "kalles_auth_token=" + authCookie)
        );
    }

    @Quando("ele cadastrar um produto na filial ativa")
    public void whenCreatingProductInActiveCompany() {
        response = givenAuthenticated(authContext, companyAId)
                .body(Map.of(
                        "name", "Arroz Tipo 1",
                        "internalCode", "ARZ-001",
                        "barcode", "789100000001",
                        "description", "Pacote 5kg",
                        "price", 32.90,
                        "costPrice", 24.50
                ))
                .when()
                .post("/api/products");
    }

    @Quando("ele consultar o deposito externo no contexto da filial ativa")
    public void whenFetchingForeignWarehouseFromActiveCompany() {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/warehouses/" + foreignWarehouseId,
                Map.of(
                        "Cookie", "kalles_auth_token=" + authCookie,
                        "X-Company-ID", companyAId.toString()
                )
        );
    }

    @Quando("ele tentar registrar estoque para o produto externo")
    public void whenSettingStockForForeignTenantProduct() {
        response = givenAuthenticated(authContext, companyAId)
                .body(Map.of(
                        "productId", foreignProductId,
                        "locationId", locationId,
                        "quantity", 10,
                        "unitCost", 5.00
                ))
                .when()
                .post("/api/stocks");
    }

    @Entao("a resposta de inventory deve ter status HTTP {int}")
    public void thenInventoryResponseShouldHaveStatus(int statusCode) {
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve informar que a filial ativa e obrigatoria para inventory")
    public void thenInventoryShouldInformCompanyIsRequired() {
        assertThat(response.jsonPath().getString("code")).isEqualTo("COMPANY_CONTEXT_REQUIRED");
    }

    @Entao("o produto criado deve retornar o codigo interno {string}")
    public void thenCreatedProductShouldReturnInternalCode(String internalCode) {
        assertThat(response.jsonPath().getString("internalCode")).isEqualTo(internalCode);
    }
}
