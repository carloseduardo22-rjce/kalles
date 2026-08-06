package dev.kalles.sale.report.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.cashregister.repository.CashRegisterSessionRepository;
import dev.kalles.sale.cashregister.repository.OperatorRepository;
import dev.kalles.sale.core.entity.Location;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Sale;
import dev.kalles.sale.core.entity.StockEntry;
import dev.kalles.sale.core.entity.Warehouse;
import dev.kalles.sale.core.enums.operator.PermissionLevel;
import dev.kalles.sale.core.repository.LocationRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.SaleRepository;
import dev.kalles.sale.core.repository.StockEntryRepository;
import dev.kalles.sale.core.repository.WarehouseRepository;
import dev.kalles.sale.core.state.CompletedState;
import dev.kalles.sale.security.support.AbstractCompanyContextApiSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class AbstractFinancialReportApiSupport extends AbstractCompanyContextApiSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected WarehouseRepository warehouseRepository;

    @Autowired
    protected LocationRepository locationRepository;

    @Autowired
    protected StockEntryRepository stockEntryRepository;

    @Autowired
    protected CashRegisterRepository cashRegisterRepository;

    @Autowired
    protected OperatorRepository operatorRepository;

    @Autowired
    protected CashRegisterSessionRepository cashRegisterSessionRepository;

    @Autowired
    protected SaleRepository saleRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void resetFinancialReportScenario() {
        resetScenarioData();
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
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

    protected Product seedProduct(UUID tenantId, String name, String internalCode) {
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName(name);
        product.setInternalCode(internalCode);
        product.setBarcode(internalCode + "-BAR");
        return productRepository.save(product);
    }

    protected Location seedLocation(UUID companyId, String warehouseName, String locationCode) {
        Warehouse warehouse = warehouseRepository.save(new Warehouse(null, warehouseName, companyId, null, true));
        Location location = new Location();
        location.setWarehouse(warehouse);
        location.setCode(locationCode);
        location.setDescription("Localizacao " + locationCode);
        return locationRepository.save(location);
    }

    protected StockEntry seedStockEntry(UUID companyId, Product product, Location location, int quantity, String unitCost, LocalDateTime createdAt) {
        StockEntry stockEntry = new StockEntry();
        stockEntry.setCompanyId(companyId);
        stockEntry.setProduct(product);
        stockEntry.setLocation(location);
        stockEntry.setQuantityAdded(quantity);
        stockEntry.setUnitCost(new BigDecimal(unitCost));
        stockEntry.setTotalCost(new BigDecimal(unitCost).multiply(BigDecimal.valueOf(quantity)));
        stockEntry = stockEntryRepository.save(stockEntry);
        jdbcTemplate.update("update stock_entries set created_at = ? where id = ?", createdAt, stockEntry.getId());
        return stockEntry;
    }

    protected Sale seedCompletedSale(UUID companyId, String cashRegisterCode, String operatorCode, String total, LocalDateTime openedAt) {
        CashRegister cashRegister = cashRegisterRepository.save(new CashRegister(cashRegisterCode, "Caixa " + cashRegisterCode, companyId));

        Operator operator = new Operator();
        operator.setCompanyId(companyId);
        operator.setName("Operador " + operatorCode);
        operator.setCode(operatorCode);
        operator.setPermissionLevel(PermissionLevel.MANAGER);
        operator.setActive(true);
        operator = operatorRepository.save(operator);

        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, new BigDecimal("100.00"));
        session = cashRegisterSessionRepository.save(session);
        jdbcTemplate.update("update cash_register_sessions set opened_at = ? where id = ?", openedAt, session.getId());

        Sale sale = new Sale();
        sale.setSessionToken(session.getId().toString());
        sale.setCompanyId(companyId);
        sale.setState(new CompletedState());
        sale.setSubtotal(new BigDecimal(total));
        sale.setTotal(new BigDecimal(total));
        sale.setAmountDue(BigDecimal.ZERO);
        sale.setFidelityDiscountApplied(BigDecimal.ZERO);
        sale.setPointsEarned(0);
        return saleRepository.save(sale);
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
                throw new IllegalStateException("Falha ao obter token CSRF para os testes de report.");
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String csrfToken = body.path("token").asText();
            String csrfCookie = response.headers()
                    .allValues("set-cookie")
                    .stream()
                    .map(AbstractFinancialReportApiSupport::extractCookieValue)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(null);

            return new CsrfContext(csrfCookie, csrfToken);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler a resposta CSRF de report.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requisicao CSRF interrompida durante os testes de report.", e);
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
