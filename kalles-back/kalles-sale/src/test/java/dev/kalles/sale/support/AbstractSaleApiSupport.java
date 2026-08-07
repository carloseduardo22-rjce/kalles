package dev.kalles.sale.support;

import dev.kalles.cashregister.entity.Operator;
import dev.kalles.client.entity.Client;
import dev.kalles.client.repository.ClientRepository;
import dev.kalles.core.entity.CompanyProduct;
import dev.kalles.core.entity.Product;
import dev.kalles.core.enums.operator.PermissionLevel;
import dev.kalles.core.repository.CompanyProductRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.core.repository.SaleRepository;
import dev.kalles.fidelity.entity.Fidelity;
import dev.kalles.fidelity.entity.FidelityPolicy;
import dev.kalles.fidelity.enums.FidelityDiscountType;
import dev.kalles.fidelity.repository.FidelityPolicyRepository;
import dev.kalles.fidelity.repository.FidelityRepository;
import dev.kalles.inventory.entity.Location;
import dev.kalles.inventory.entity.Stock;
import dev.kalles.inventory.entity.Warehouse;
import dev.kalles.inventory.repository.LocationRepository;
import dev.kalles.inventory.repository.StockRepository;
import dev.kalles.inventory.repository.WarehouseRepository;
import dev.kalles.cashregister.support.AbstractCashRegisterApiSupport;
import dev.kalles.payment.support.LocalHttpTestClient;
import dev.kalles.payment.support.RestAssuredResponseAdapter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractSaleApiSupport extends AbstractCashRegisterApiSupport {

    private static final HttpClient PATCH_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    protected static final String PRODUCT_INTERNAL_CODE = "SKU-001";
    protected static final String PRODUCT_BARCODE = "7891234567890";
    protected static final String BASIC_OPERATOR_CODE = "OP-BASIC";
    protected static final String SUPERVISOR_OPERATOR_CODE = "OP-SUP";
    protected static final String CLIENT_CPF = "12345678901";

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CompanyProductRepository companyProductRepository;

    @Autowired
    protected WarehouseRepository warehouseRepository;

    @Autowired
    protected LocationRepository locationRepository;

    @Autowired
    protected StockRepository stockRepository;

    @Autowired
    protected SaleRepository saleRepository;

    @Autowired
    protected ClientRepository clientRepository;

    @Autowired
    protected FidelityRepository fidelityRepository;

    @Autowired
    protected FidelityPolicyRepository fidelityPolicyRepository;

    protected Product seededProduct;
    protected Client seededClient;
    protected FidelityPolicy seededFidelityPolicy;
    protected Fidelity seededFidelity;
    protected UUID basicOperatorId;
    protected UUID supervisorAuthorizerId;

    protected void prepareSaleScenario(boolean paymentIntegrationConfigured) {
        resetScenarioData();
        configurePaymentIntegration(paymentIntegrationConfigured);
        elevateDefaultOperator();
        seedAuthorizationOperators();
        seededProduct = seedProductCatalog(new BigDecimal("30.00"), new BigDecimal("18.00"), 20);
    }

    protected void elevateDefaultOperator() {
        Operator operator = operatorRepository.findByCodeAndCompanyId(OPERATOR_CODE, companyId)
                .orElseThrow(() -> new IllegalStateException("Operador de teste nao encontrado."));
        operator.setPermissionLevel(PermissionLevel.SUPERVISOR);
        operatorRepository.save(operator);
    }

    protected void seedAuthorizationOperators() {
        Operator basicOperator = new Operator("Operador Basico", BASIC_OPERATOR_CODE);
        basicOperator.setCompanyId(companyId);
        basicOperator.setPermissionLevel(PermissionLevel.BASIC);
        basicOperator = operatorRepository.save(basicOperator);
        basicOperatorId = basicOperator.getId();

        Operator supervisorOperator = new Operator("Supervisor Autorizador", SUPERVISOR_OPERATOR_CODE);
        supervisorOperator.setCompanyId(companyId);
        supervisorOperator.setPermissionLevel(PermissionLevel.SUPERVISOR);
        supervisorOperator = operatorRepository.save(supervisorOperator);
        supervisorAuthorizerId = supervisorOperator.getId();
    }

    protected Product seedProductCatalog(BigDecimal salePrice, BigDecimal costPrice, int stockQuantity) {
        Product product = new Product();
        product.setTenantId(TENANT_ID);
        product.setName("Produto PDV");
        product.setInternalCode(PRODUCT_INTERNAL_CODE);
        product.setBarcode(PRODUCT_BARCODE);
        product.setDescription("Produto para testes do fluxo de venda");
        product = productRepository.save(product);

        CompanyProduct companyProduct = new CompanyProduct();
        companyProduct.setCompanyId(companyId);
        companyProduct.setProduct(product);
        companyProduct.setPrice(salePrice);
        companyProduct.setCostPrice(costPrice);
        companyProduct.setActive(true);
        companyProductRepository.save(companyProduct);

        Warehouse warehouse = warehouseRepository.save(new Warehouse(
                null,
                "Deposito PDV",
                companyId,
                "Rua Teste, 100",
                true
        ));
        Location location = locationRepository.save(new Location(
                null,
                warehouse,
                "A-01",
                "Prateleira principal"
        ));
        stockRepository.save(new Stock(product, location, stockQuantity));
        return product;
    }

    protected Client seedClient() {
        Client client = new Client();
        client.setCompanyId(companyId);
        client.setName("Cliente Fidelidade");
        client.setCpf(CLIENT_CPF);
        client.setBirthDate(LocalDate.of(1990, 1, 1));
        client.setCellphone("11999999999");
        seededClient = clientRepository.save(client);
        return seededClient;
    }

    protected FidelityPolicy seedActiveFidelityPolicy(
            BigDecimal configuredDiscount,
            FidelityDiscountType discountType,
            int objectivePoints,
            int valuePoint) {
        FidelityPolicy policy = new FidelityPolicy();
        policy.setCompanyId(companyId);
        policy.setConfiguredDiscount(configuredDiscount);
        policy.setDiscountType(discountType);
        policy.setObjectivePoints(objectivePoints);
        policy.setValuePoint(valuePoint);
        policy.setActive(true);
        policy.setCreatedAt(LocalDate.now());
        seededFidelityPolicy = fidelityPolicyRepository.save(policy);
        return seededFidelityPolicy;
    }

    protected Fidelity seedClientWithAvailableFidelityDiscount(BigDecimal availableDiscount) {
        Client client = seedClient();
        FidelityPolicy policy = seedActiveFidelityPolicy(
                availableDiscount,
                FidelityDiscountType.FIXED,
                100,
                1
        );
        Fidelity fidelity = new Fidelity();
        fidelity.setClient(client);
        fidelity.setPolicy(policy);
        fidelity.setPoints(0);
        fidelity.setAvailableDiscount(availableDiscount);
        fidelity.setCreatedAt(LocalDate.now());
        fidelity.setExpired(false);
        seededFidelity = fidelityRepository.save(fidelity);
        return seededFidelity;
    }

    protected String openSession(AuthContext authContext, boolean cashOnlyOperation) {
        Response response = given()
                .contentType(ContentType.JSON)
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "cashRegisterCode", CASH_REGISTER_CODE,
                        "operatorCode", OPERATOR_CODE,
                        "initialAmount", 100.00,
                        "allowCashOnlyOperation", cashOnlyOperation
                ))
                .when()
                .post("/api/cash-register-sessions/open");

        response.then().statusCode(201);
        return response.jsonPath().getString("sessionId");
    }

    protected Response patchJson(AuthContext authContext, String path, String body) {
        return patchJson(authContext, path, body, Map.of());
    }

    protected Response patchJson(AuthContext authContext, String path, String body, Map<String, String> extraHeaders) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", ContentType.JSON.toString())
                    .header("Accept", ContentType.JSON.toString())
                    .header(
                            "Cookie",
                            "kalles_auth_token=" + authContext.authCookie()
                                    + "; XSRF-TOKEN=" + authContext.csrfCookie()
                    )
                    .header("X-XSRF-TOKEN", authContext.csrfToken())
                    .header("X-Company-ID", companyId.toString())
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body));

            extraHeaders.forEach(requestBuilder::header);

            HttpResponse<String> response = PATCH_HTTP_CLIENT.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return RestAssuredResponseAdapter.from(response.statusCode(), response.headers().map(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fail to execute sale PATCH request for tests", e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to execute sale PATCH request for tests", e);
        }
    }

    protected Response deleteWithHeaders(AuthContext authContext, String path, Map<String, String> extraHeaders) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", ContentType.JSON.toString())
                    .header(
                            "Cookie",
                            "kalles_auth_token=" + authContext.authCookie()
                                    + "; XSRF-TOKEN=" + authContext.csrfCookie()
                    )
                    .header("X-XSRF-TOKEN", authContext.csrfToken())
                    .header("X-Company-ID", companyId.toString())
                    .DELETE();

            extraHeaders.forEach(requestBuilder::header);

            HttpResponse<String> response = PATCH_HTTP_CLIENT.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return RestAssuredResponseAdapter.from(response.statusCode(), response.headers().map(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fail to execute sale DELETE request for tests", e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to execute sale DELETE request for tests", e);
        }
    }

    protected Response getJson(AuthContext authContext, String path) {
        return LocalHttpTestClient.get(
                "http://localhost:" + port + path,
                Map.of(
                        "Accept", "application/json",
                        "Cookie", "kalles_auth_token=" + authContext.authCookie() + "; XSRF-TOKEN=" + authContext.csrfCookie(),
                        "X-XSRF-TOKEN", authContext.csrfToken(),
                        "X-Company-ID", companyId.toString()
                )
        );
    }
}
