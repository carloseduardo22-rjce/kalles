package dev.kalles.sale.inventory.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.sale.core.entity.CompanyProduct;
import dev.kalles.sale.core.entity.Location;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Stock;
import dev.kalles.sale.core.entity.Warehouse;
import dev.kalles.sale.core.repository.CompanyProductRepository;
import dev.kalles.sale.core.repository.LocationRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.StockRepository;
import dev.kalles.sale.core.repository.WarehouseRepository;
import dev.kalles.sale.security.support.AbstractCompanyContextApiSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractInventoryApiSupport extends AbstractCompanyContextApiSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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

    protected void resetInventoryScenario() {
        resetScenarioData();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        stockRepository.deleteAll();
        locationRepository.deleteAll();
        warehouseRepository.deleteAll();
        companyProductRepository.deleteAll();
        productRepository.deleteAll();
    }

    protected AuthContext authenticateTenantAdminWithCsrf() {
        String authCookie = loginAndExtractAuthCookie(TENANT_ADMIN_EMAIL);
        CsrfContext csrf = fetchCsrfToken();
        return new AuthContext(authCookie, csrf.csrfCookie(), csrf.csrfToken());
    }

    protected RequestSpecification givenAuthenticated(AuthContext authContext, UUID companyId) {
        return given()
                .cookie("kalles_auth_token", authContext.authCookie())
                .cookie("XSRF-TOKEN", authContext.csrfCookie())
                .header("X-XSRF-TOKEN", authContext.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .contentType(ContentType.JSON);
    }

    protected UUID seedProduct(UUID tenantId, UUID companyId, String internalCode, String barcode) {
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName("Produto " + internalCode);
        product.setInternalCode(internalCode);
        product.setBarcode(barcode);
        product.setDescription("Produto de teste " + internalCode);
        product = productRepository.save(product);

        CompanyProduct companyProduct = new CompanyProduct();
        companyProduct.setCompanyId(companyId);
        companyProduct.setProduct(product);
        companyProduct.setPrice(new BigDecimal("30.00"));
        companyProduct.setCostPrice(new BigDecimal("20.00"));
        companyProduct.setActive(true);
        companyProductRepository.save(companyProduct);
        return product.getId();
    }

    protected UUID seedWarehouse(UUID companyId, String name) {
        Warehouse warehouse = warehouseRepository.save(new Warehouse(null, name, companyId, "Rua Teste", true));
        return warehouse.getId();
    }

    protected UUID seedLocation(UUID companyId, String code) {
        Warehouse warehouse = warehouseRepository.save(new Warehouse(null, "Deposito " + code, companyId, "Rua Teste", true));
        Location location = locationRepository.save(new Location(null, warehouse, code, "Localizacao " + code));
        return location.getId();
    }

    protected void seedStock(UUID productId, UUID locationId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Produto de teste nao encontrado."));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalStateException("Localizacao de teste nao encontrada."));
        stockRepository.save(new Stock(product, location, quantity));
    }

    protected record AuthContext(String authCookie, String csrfCookie, String csrfToken) {
    }

    protected record CsrfContext(String csrfCookie, String csrfToken) {
    }

    private CsrfContext fetchCsrfToken() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/csrf"))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Falha ao obter token CSRF para os testes de inventory.");
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String csrfToken = body.path("token").asText();
            String csrfCookie = response.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(AbstractInventoryApiSupport::extractCookieValue)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(null);

            return new CsrfContext(csrfCookie, csrfToken);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a resposta CSRF de inventory.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requisicao CSRF interrompida durante os testes de inventory.", e);
        }
    }

    private static Optional<String> extractCookieValue(String headerValue) {
        if (headerValue == null || !headerValue.startsWith("XSRF-TOKEN=")) {
            return Optional.empty();
        }

        int separator = headerValue.indexOf(';');
        String cookie = separator >= 0 ? headerValue.substring(0, separator) : headerValue;
        return Optional.of(cookie.substring("XSRF-TOKEN=".length()));
    }
}
