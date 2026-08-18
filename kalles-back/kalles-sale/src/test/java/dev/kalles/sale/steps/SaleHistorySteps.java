package dev.kalles.sale.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.entity.CashRegisterSession;
import dev.kalles.cashregister.entity.Operator;
import dev.kalles.cashregister.enums.PermissionLevel;
import dev.kalles.company.entity.Company;
import dev.kalles.company.entity.Tenant;
import dev.kalles.testsupport.LocalHttpTestClient;
import dev.kalles.testsupport.RestAssuredResponseAdapter;
import dev.kalles.testsupport.TestHttpTimeout;
import dev.kalles.product.entity.Product;
import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.entity.Sale;
import dev.kalles.sale.enums.PaymentMethod;
import dev.kalles.sale.state.CanceledState;
import dev.kalles.sale.state.CompletedState;
import dev.kalles.sale.state.OpenState;
import dev.kalles.security.entity.Account;
import dev.kalles.security.enums.AccountRole;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;

public class SaleHistorySteps extends SaleCucumberSpringConfiguration {

    private static final UUID BETA_TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174222");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(TestHttpTimeout.CONNECT)
            .build();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Map<String, UUID> companyIds = new LinkedHashMap<>();
    private final Map<String, UUID> tenantIds = new LinkedHashMap<>();
    private AuthContext authContext;
    private UUID activeCompanyId;
    private Response lastResponse;
    private byte[] lastExport;
    private int productSequence;

    @Before
    public void beforeSaleHistoryScenario() {
        companyIds.clear();
        tenantIds.clear();
        authContext = null;
        activeCompanyId = null;
        lastResponse = null;
        lastExport = null;
        productSequence = 0;
    }

    @Dado("que existem dois tenants cadastrados no Kalles")
    public void givenTwoTenants() {
        prepareSaleScenario(true);

        Company alpha = companyRepository.findById(companyId).orElseThrow();
        alpha.setName("Matriz Alpha");
        companyRepository.save(alpha);

        tenantIds.put("Alpha", TENANT_ID);
        companyIds.put("Matriz Alpha", companyId);

        tenantRepository.save(new Tenant(BETA_TENANT_ID, "Tenant Beta"));
        UUID betaCompanyId = companyRepository.save(new Company(
                null,
                "Matriz Beta",
                BETA_TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();
        companyIds.put("Matriz Beta", betaCompanyId);
        tenantIds.put("Beta", BETA_TENANT_ID);

        cashRegisterRepository.save(new CashRegister("CAIXA-BETA", "Caixa Beta", betaCompanyId));
        Operator betaOperator = new Operator("Operador Beta", "OP-BETA");
        betaOperator.setCompanyId(betaCompanyId);
        betaOperator.setPermissionLevel(PermissionLevel.SUPERVISOR);
        operatorRepository.save(betaOperator);
    }

    @Dado("o tenant {string} possui a filial {string}")
    public void givenTenantHasCompany(String tenantName, String companyName) {
        assertThat(tenantIds).containsKey(tenantName);
        assertThat(companyIds).containsKey(companyName);
    }

    @Dado("existe um operador autenticado para cada tenant")
    public void givenAuthenticatedOperatorsExist() {
        assertThat(operatorRepository.findByCodeAndCompanyId(OPERATOR_CODE, companyIds.get("Matriz Alpha"))).isPresent();
        assertThat(operatorRepository.findByCodeAndCompanyId("OP-BETA", companyIds.get("Matriz Beta"))).isPresent();
    }

    @Dado("que o operador do tenant {string} esta autenticado")
    public void givenTenantOperatorIsAuthenticated(String tenantName) {
        assertThat(tenantName).isEqualTo("Alpha");
        authContext = authenticateOperator();
    }

    @Dado("a filial ativa da requisicao e {string}")
    public void givenActiveCompany(String companyName) {
        activeCompanyId = companyIds.get(companyName);
        assertThat(activeCompanyId).isNotNull();
    }

    @Dado("existe uma venda {string} de {string} na filial {string} aberta em {string}")
    public void givenSale(String state, String amount, String companyName, String openedAt) {
        seedSale(state, new BigDecimal(amount), companyName, LocalDateTime.parse(openedAt), true);
    }

    @Dado("existe uma venda {string} de {string} na filial {string} com itens e pagamentos em {string}")
    public void givenSaleWithItemsAndPayments(String state, String amount, String companyName, String openedAt) {
        seedSale(state, new BigDecimal(amount), companyName, LocalDateTime.parse(openedAt), true);
    }

    @Quando("consultar o historico de vendas de {string} ate {string}")
    public void whenListHistory(String startDate, String endDate) {
        lastResponse = getJson(
                "/api/sales/history?startDate=" + startDate + "&endDate=" + endDate,
                true
        );
    }

    @Quando("consultar o historico de vendas de {string} ate {string} com estado {string}")
    public void whenListHistoryByState(String startDate, String endDate, String state) {
        lastResponse = getJson(
                "/api/sales/history?startDate=" + startDate + "&endDate=" + endDate + "&state=" + state,
                true
        );
    }

    @Quando("consultar o historico de vendas de {string} ate {string} sem informar filial ativa")
    public void whenListHistoryWithoutCompany(String startDate, String endDate) {
        Account account = accountRepository.findAllByEmailIgnoreCase(OPERATOR_EMAIL).getFirst();
        account.setCompanyId(null);
        account.setRole(AccountRole.ADMIN);
        accountRepository.save(account);
        posDeviceSessionRepository.deleteAll();
        authContext = authenticateOperator();

        lastResponse = getJson(
                "/api/sales/history?startDate=" + startDate + "&endDate=" + endDate,
                false
        );
    }

    @Quando("exportar o historico de vendas de {string} ate {string} para Excel")
    public void whenExportHistory(String startDate, String endDate) {
        RawHttpResponse response = getBytes(
                "/api/sales/history/export?startDate=" + startDate + "&endDate=" + endDate
        );
        lastResponse = RestAssuredResponseAdapter.from(response.statusCode(), response.headers(), "");
        lastExport = response.body();
    }

    @Entao("a operacao deve responder com status HTTP {int}")
    public void thenOperationShouldReturnStatus(int statusCode) {
        assertThat(lastResponse.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve conter {int} vendas")
    public void thenResponseShouldContainSales(int count) {
        assertThat(lastResponse.jsonPath().getList("$")).hasSize(count);
    }

    @Entao("a resposta deve conter {int} venda")
    public void thenResponseShouldContainOneSale(int count) {
        thenResponseShouldContainSales(count);
    }

    @Entao("a primeira venda deve possuir estado {string}")
    public void thenFirstSaleShouldHaveState(String state) {
        assertThat(lastResponse.jsonPath().getString("[0].state")).isEqualTo(state);
    }

    @Entao("a segunda venda deve possuir estado {string}")
    public void thenSecondSaleShouldHaveState(String state) {
        assertThat(lastResponse.jsonPath().getString("[1].state")).isEqualTo(state);
    }

    @Entao("cada venda deve informar id, sessionToken, companyId, state, subtotal, total, amountDue, fidelityDiscountApplied e pointsEarned")
    public void thenEachSaleShouldContainCoreFields() {
        int count = lastResponse.jsonPath().getList("$").size();
        for (int i = 0; i < count; i++) {
            assertThat(lastResponse.jsonPath().getString("[%d].id".formatted(i))).isNotBlank();
            assertThat(lastResponse.jsonPath().getString("[%d].sessionToken".formatted(i))).isNotBlank();
            assertThat(lastResponse.jsonPath().getString("[%d].companyId".formatted(i))).isNotBlank();
            assertThat(lastResponse.jsonPath().getString("[%d].state".formatted(i))).isNotBlank();
            assertThat(lastResponse.jsonPath().getString("[%d].subtotal".formatted(i))).isNotNull();
            assertThat(lastResponse.jsonPath().getString("[%d].total".formatted(i))).isNotNull();
            assertThat(lastResponse.jsonPath().getString("[%d].amountDue".formatted(i))).isNotNull();
            assertThat(lastResponse.jsonPath().getString("[%d].fidelityDiscountApplied".formatted(i))).isNotNull();
            assertThat(lastResponse.jsonPath().getString("[%d].pointsEarned".formatted(i))).isNotNull();
        }
    }

    @Entao("cada venda deve informar seus itens e pagamentos vinculados")
    public void thenEachSaleShouldContainItemsAndPayments() {
        int count = lastResponse.jsonPath().getList("$").size();
        for (int i = 0; i < count; i++) {
            assertThat(lastResponse.jsonPath().getList("[%d].items".formatted(i))).isNotEmpty();
            assertThat(lastResponse.jsonPath().getList("[%d].payments".formatted(i))).isNotEmpty();
        }
    }

    @Entao("a venda retornada deve possuir estado {string}")
    public void thenReturnedSaleShouldHaveState(String state) {
        assertThat(lastResponse.jsonPath().getString("[0].state")).isEqualTo(state);
    }

    @Entao("a resposta deve informar {string}")
    public void thenResponseShouldInform(String message) {
        assertThat(lastResponse.jsonPath().getString("detail")).isEqualTo(message);
    }

    @Entao("a venda retornada deve pertencer a filial {string}")
    public void thenReturnedSaleShouldBelongToCompany(String companyName) {
        assertThat(lastResponse.jsonPath().getString("[0].companyId"))
                .isEqualTo(companyIds.get(companyName).toString());
    }

    @Entao("a resposta nao deve conter vendas do tenant {string}")
    public void thenResponseShouldNotContainTenantSales(String tenantName) {
        UUID leakedCompanyId = companyIds.get("Matriz " + tenantName);
        int count = lastResponse.jsonPath().getList("$").size();
        for (int i = 0; i < count; i++) {
            assertThat(lastResponse.jsonPath().getString("[%d].companyId".formatted(i)))
                    .isNotEqualTo(leakedCompanyId.toString());
        }
    }

    @Entao("o arquivo deve ser retornado com tipo {string}")
    public void thenFileShouldHaveContentType(String contentType) {
        assertThat(lastResponse.contentType()).isEqualTo(contentType);
    }

    @Entao("o arquivo deve possuir a aba {string}")
    public void thenFileShouldContainSheet(String sheetName) throws IOException {
        assertThat(unzip(lastExport).get("xl/workbook.xml")).contains("name=\"" + sheetName + "\"");
    }

    @Entao("a aba {string} deve possuir as colunas {string}")
    public void thenSheetShouldContainColumns(String sheetName, String columns) throws IOException {
        String sheet = switch (sheetName) {
            case "sales" -> "xl/worksheets/sheet1.xml";
            case "sale_items" -> "xl/worksheets/sheet2.xml";
            case "payments" -> "xl/worksheets/sheet3.xml";
            default -> throw new IllegalArgumentException("Aba desconhecida: " + sheetName);
        };

        String content = unzip(lastExport).get(sheet);
        for (String column : columns.split(",")) {
            assertThat(content).contains(column.trim().replace("\"", ""));
        }
    }

    @Entao("nenhuma aba deve conter vendas, itens ou pagamentos de outro tenant")
    public void thenExportShouldNotContainAnotherTenant() throws IOException {
        String betaCompanyId = companyIds.get("Matriz Beta").toString();
        Map<String, String> entries = unzip(lastExport);
        assertThat(entries.get("xl/worksheets/sheet1.xml")).doesNotContain(betaCompanyId);
        assertThat(entries.get("xl/worksheets/sheet2.xml")).doesNotContain(betaCompanyId);
        assertThat(entries.get("xl/worksheets/sheet3.xml")).doesNotContain(betaCompanyId);
    }

    private Response getJson(String path, boolean withCompanyHeader) {
        return LocalHttpTestClient.get("http://localhost:" + port + path, requestHeaders(
                "application/json",
                withCompanyHeader
        ));
    }

    private RawHttpResponse getBytes(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + path))
                    .timeout(TestHttpTimeout.REQUEST)
                    .headers(flattenHeaders(requestHeaders(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            true
                    )))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return new RawHttpResponse(response.statusCode(), response.headers().map(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fail to execute sale history export request for tests", e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to execute sale history export request for tests", e);
        }
    }

    private Map<String, String> requestHeaders(String accept, boolean withCompanyHeader) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", accept);
        headers.put(
                "Cookie",
                "kalles_auth_token=" + authContext.authCookie() + "; XSRF-TOKEN=" + authContext.csrfCookie()
        );
        headers.put("X-XSRF-TOKEN", authContext.csrfToken());
        if (withCompanyHeader) {
            headers.put("X-Company-ID", activeCompanyId.toString());
        }
        return headers;
    }

    private String[] flattenHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .flatMap(entry -> java.util.stream.Stream.of(entry.getKey(), entry.getValue()))
                .toArray(String[]::new);
    }

    private void seedSale(String state, BigDecimal amount, String companyName, LocalDateTime openedAt, boolean withPayment) {
        UUID targetCompanyId = companyIds.get(companyName);
        UUID targetTenantId = companyName.endsWith("Beta") ? BETA_TENANT_ID : TENANT_ID;
        CashRegister cashRegister = cashRegisterRepository.findAll().stream()
                .filter(register -> register.getCompanyId().equals(targetCompanyId))
                .findFirst()
                .orElseGet(() -> cashRegisterRepository.save(new CashRegister("CAIXA-" + companyName, "Caixa", targetCompanyId)));
        Operator operator = operatorRepository.findAll().stream()
                .filter(candidate -> candidate.getCompanyId().equals(targetCompanyId))
                .findFirst()
                .orElseThrow();

        CashRegisterSession session = CashRegisterSession.open(cashRegister, operator, BigDecimal.ZERO);
        ReflectionTestUtils.setField(session.getSessionPeriod(), "openedAt", openedAt);
        session.close();
        session = cashRegisterSessionRepository.save(session);

        Product product = new Product();
        product.setTenantId(targetTenantId);
        product.setName("Produto Historico " + (++productSequence));
        product.setInternalCode("HIST-" + productSequence);
        product = productRepository.save(product);

        Sale sale = new Sale();
        sale.setSessionToken(session.getId().toString());
        sale.setCompanyId(targetCompanyId);
        sale.setState(new OpenState());
        sale.addItem(product, amount);
        sale.startPayment();
        if (withPayment) {
            sale.addPayment(new Payment(sale, PaymentMethod.CASH, amount, BigDecimal.ZERO, "hist-" + productSequence, true));
        }
        if ("COMPLETED".equals(state)) {
            sale.setState(new CompletedState());
        } else if ("CANCELED".equals(state)) {
            sale.setState(new CanceledState());
        } else {
            throw new IllegalArgumentException("Estado de venda nao suportado no teste: " + state);
        }
        sale = saleRepository.save(sale);
        if ("COMPLETED".equals(state)) {
            ReflectionTestUtils.setField(sale, "completedAt", openedAt);
            saleRepository.save(sale);
        }
        jdbcTemplate.update("UPDATE sale SET created_at = ? WHERE id = ?", openedAt, sale.getId());
    }

    private Map<String, String> unzip(byte[] content) throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }

    private record RawHttpResponse(int statusCode, Map<String, List<String>> headers, byte[] body) {
    }
}
