package dev.kalles.fiscal.integration;

import dev.kalles.cashregister.support.AbstractCashRegisterApiSupport;
import dev.kalles.core.entity.Product;
import dev.kalles.core.entity.Sale;
import dev.kalles.core.repository.PaymentRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.core.repository.SaleRepository;
import dev.kalles.core.state.CompletedState;
import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalDocumentEntity;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalCertificateRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalConfigurationRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalDocumentRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalIssuerAddressRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalIssuerProfileRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalProductClassificationRepository;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsMapContaining.hasKey;

@Tag("integration")
class FiscalOperationsApiIntegrationTest extends AbstractCashRegisterApiSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SpringDataFiscalDocumentRepository fiscalDocumentRepository;

    @Autowired
    private SpringDataFiscalConfigurationRepository fiscalConfigurationRepository;

    @Autowired
    private SpringDataFiscalCertificateRepository fiscalCertificateRepository;

    @Autowired
    private SpringDataFiscalProductClassificationRepository fiscalProductClassificationRepository;

    @Autowired
    private SpringDataFiscalIssuerProfileRepository fiscalIssuerProfileRepository;

    @Autowired
    private SpringDataFiscalIssuerAddressRepository fiscalIssuerAddressRepository;

    private AuthContext auth;

    @BeforeEach
    void setUp() {
        resetScenarioData();
        fiscalDocumentRepository.deleteAll();
        fiscalConfigurationRepository.deleteAll();
        fiscalCertificateRepository.deleteAll();
        fiscalProductClassificationRepository.deleteAll();
        fiscalIssuerAddressRepository.deleteAll();
        fiscalIssuerProfileRepository.deleteAll();
        paymentRepository.deleteAll();
        auth = authenticateOperator();
    }

    @Test
    void shouldConfigureFiscalCompanyInsideActiveTenantAndCompany() {
        authenticatedJson()
                .body(Map.of(
                        "model", "NFCE",
                        "environment", "HOMOLOGACAO",
                        "stateCode", "SP",
                        "cscId", "1",
                        "cscToken", "CSC-HOMOLOGACAO",
                        "series", 1,
                        "nextNumber", 100
                ))
                .when()
                .post("/api/fiscal/configurations")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("model", equalTo("NFCE"))
                .body("series", equalTo(1))
                .body("nextNumber", equalTo(100));
    }

    @Test
    void shouldPersistIssuerProfileAddressAndReadinessInsideActiveTenantAndCompany() throws Exception {
        authenticatedJson()
                .body(Map.of(
                        "cnpj", "11.222.333/0001-81",
                        "legalName", "Kalles Comercio LTDA",
                        "tradeName", "Kalles Matriz",
                        "stateRegistration", "110.042.490.114",
                        "taxRegime", "SIMPLES_NACIONAL",
                        "cnae", "4712100"
                ))
                .when()
                .post("/api/fiscal/issuer-profile")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("cnpj", equalTo("11222333000181"))
                .body("stateRegistration", equalTo("110042490114"));

        authenticatedJson()
                .body(Map.ofEntries(
                        Map.entry("zipCode", "01001-000"),
                        Map.entry("stateCode", "SP"),
                        Map.entry("stateIbgeCode", 35),
                        Map.entry("cityName", "Sao Paulo"),
                        Map.entry("cityIbgeCode", 3550308),
                        Map.entry("district", "Se"),
                        Map.entry("street", "Praca da Se"),
                        Map.entry("number", "100"),
                        Map.entry("complement", "Loja 1")
                ))
                .when()
                .post("/api/fiscal/issuer-address")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("zipCode", equalTo("01001000"))
                .body("countryName", equalTo("Brasil"))
                .body("countryCode", equalTo(1058));

        authenticatedJson()
                .body(Map.of(
                        "certificateBase64", "BASE64-PFX",
                        "password", "pfx-password",
                        "expiresAt", Instant.now().plusSeconds(3600).toString()
                ))
                .when()
                .post("/api/fiscal/certificates")
                .then()
                .statusCode(201);

        HttpResponse<String> readinessResponse = httpGetString("/api/fiscal/readiness");
        JsonNode readinessBody = OBJECT_MAPPER.readTree(readinessResponse.body());

        assertThat(readinessResponse.statusCode()).isEqualTo(200);
        assertThat(readinessBody.path("tenantId").asText()).isEqualTo(TENANT_ID.toString());
        assertThat(readinessBody.path("companyId").asText()).isEqualTo(companyId.toString());
        assertThat(readinessBody.path("ready").asBoolean()).isTrue();
        assertThat(readinessBody.path("missingItems")).isEmpty();
    }

    @Test
    void shouldSaveFiscalPreparationWithSingleRequestInsideActiveTenantAndCompany() {
        authenticatedJson()
                .body(validPreparationPayload(Instant.now().plusSeconds(3600)))
                .when()
                .post("/api/fiscal/preparation")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("ready", equalTo(true))
                .body("missingItems.size()", equalTo(0))
                .body("$", not(hasKey("certificateBase64")))
                .body("$", not(hasKey("certificatePassword")))
                .body("$", not(hasKey("cscToken")));
    }

    @Test
    void shouldRejectUnifiedFiscalPreparationWithExpiredCertificate() {
        authenticatedJson()
                .body(validPreparationPayload(Instant.now().minusSeconds(60)))
                .when()
                .post("/api/fiscal/preparation")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Certificado digital expirado"));

        assertThat(fiscalCertificateRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectIssuerProfileWithInvalidCnpj() {
        authenticatedJson()
                .body(Map.of(
                        "cnpj", "11.111.111/1111-11",
                        "legalName", "Kalles Comercio LTDA",
                        "tradeName", "Kalles Matriz",
                        "stateRegistration", "110042490114",
                        "taxRegime", "SIMPLES_NACIONAL",
                        "cnae", "4712100"
                ))
                .when()
                .post("/api/fiscal/issuer-profile")
                .then()
                .statusCode(400)
                .body("detail", equalTo("CNPJ do emissor fiscal invalido"));
    }

    @Test
    void shouldRegisterCertificateWithoutExposingProtectedPayload() {
        authenticatedJson()
                .body(Map.of(
                        "certificateBase64", "BASE64-PFX",
                        "password", "pfx-password",
                        "expiresAt", Instant.now().plusSeconds(3600).toString()
                ))
                .when()
                .post("/api/fiscal/certificates")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("active", equalTo(true))
                .body("$", not(hasKey("password")))
                .body("$", not(hasKey("certificateBase64")));
    }

    @Test
    void shouldRejectExpiredCertificateRegistration() {
        authenticatedJson()
                .body(Map.of(
                        "certificateBase64", "BASE64-PFX",
                        "password", "pfx-password",
                        "expiresAt", Instant.now().minusSeconds(60).toString()
                ))
                .when()
                .post("/api/fiscal/certificates")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Certificado digital expirado"));
    }

    @Test
    void shouldPersistProductFiscalClassificationForCurrentTenant() {
        UUID productId = seedProduct(TENANT_ID);

        authenticatedJson()
                .body(Map.ofEntries(
                        Map.entry("productId", productId.toString()),
                        Map.entry("ncm", "61091000"),
                        Map.entry("cest", "2805800"),
                        Map.entry("cfop", "5102"),
                        Map.entry("cfopSale", "5102"),
                        Map.entry("origin", "0"),
                        Map.entry("csosn", "102"),
                        Map.entry("unit", "UN"),
                        Map.entry("gtin", "7890000000000")
                ))
                .when()
                .post("/api/fiscal/product-classifications")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("productId", equalTo(productId.toString()))
                .body("ncm", equalTo("61091000"))
                .body("cfopSale", equalTo("5102"))
                .body("csosn", equalTo("102"))
                .body("unit", equalTo("UN"));
    }

    @Test
    void shouldRejectProductFiscalClassificationIncompatibleWithIssuerRegime() {
        UUID productId = seedProduct(TENANT_ID);

        authenticatedJson()
                .body(Map.of(
                        "cnpj", "11.222.333/0001-81",
                        "legalName", "Kalles Comercio LTDA",
                        "tradeName", "Kalles Matriz",
                        "stateRegistration", "110042490114",
                        "taxRegime", "SIMPLES_NACIONAL",
                        "cnae", "4712100"
                ))
                .when()
                .post("/api/fiscal/issuer-profile")
                .then()
                .statusCode(201);

        authenticatedJson()
                .body(Map.of(
                        "productId", productId.toString(),
                        "ncm", "61091000",
                        "cfop", "5102",
                        "cst", "00"
                ))
                .when()
                .post("/api/fiscal/product-classifications")
                .then()
                .statusCode(400)
                .body("detail", equalTo("Tributacao do produto incompativel com o regime fiscal da filial"));
    }

    @Test
    void shouldRejectProductClassificationForForeignProduct() {
        UUID foreignProductId = seedProduct(UUID.fromString("123e4567-e89b-12d3-a456-426614174311"));

        authenticatedJson()
                .body(Map.of(
                        "productId", foreignProductId.toString(),
                        "ncm", "61091000",
                        "cfop", "5102"
                ))
                .when()
                .post("/api/fiscal/product-classifications")
                .then()
                .statusCode(404)
                .body("detail", equalTo("Produto nao encontrado"));
    }

    @Test
    void shouldQueryStatusAndDownloadDanfeOnlyForAuthorizedDocument() throws Exception {
        UUID saleId = seedSale();
        UUID documentId = seedDocument(saleId, FiscalDocumentStatus.AUTORIZADO);

        HttpResponse<String> statusResponse = httpGetString("/api/fiscal/documents/" + documentId + "/status");
        JsonNode statusBody = OBJECT_MAPPER.readTree(statusResponse.body());

        assertThat(statusResponse.statusCode()).isEqualTo(200);
        assertThat(statusBody.path("id").asText()).isEqualTo(documentId.toString());
        assertThat(statusBody.path("status").asText()).isEqualTo("AUTORIZADO");
        assertThat(statusBody.path("accessKey").asText()).isNotBlank();

        HttpResponse<byte[]> danfeResponse = httpGetBytes("/api/fiscal/documents/" + documentId + "/danfe");
        assertThat(danfeResponse.statusCode()).isEqualTo(200);
        assertThat(danfeResponse.headers().firstValue("content-type")).contains("application/pdf");
        assertThat(new String(danfeResponse.body(), java.nio.charset.StandardCharsets.UTF_8)).startsWith("%PDF");
    }

    @Test
    void shouldRejectDanfeForRejectedDocument() throws Exception {
        UUID saleId = seedSale();
        UUID documentId = seedDocument(saleId, FiscalDocumentStatus.REJEITADO);

        HttpResponse<String> response = httpGetString("/api/fiscal/documents/" + documentId + "/danfe");
        JsonNode body = OBJECT_MAPPER.readTree(response.body());

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(body.path("detail").asText()).isEqualTo("DANFE disponivel apenas para documento autorizado");
    }

    @Test
    void shouldIssueFiscalReturnOnlyAfterAuthorizedNfceAndRefund() {
        UUID saleId = seedSale();
        seedDocument(saleId, FiscalDocumentStatus.AUTORIZADO);
        seedRefund(saleId);

        authenticatedJson()
                .body(Map.of("saleId", saleId.toString()))
                .when()
                .post("/api/fiscal/returns/issue")
                .then()
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("saleId", equalTo(saleId.toString()))
                .body("model", equalTo("NFE_DEVOLUCAO"))
                .body("status", equalTo("AUTORIZADO"))
                .body("accessKey", notNullValue());
    }

    @Test
    void shouldRejectFiscalReturnWithoutRefund() {
        UUID saleId = seedSale();
        seedDocument(saleId, FiscalDocumentStatus.AUTORIZADO);

        authenticatedJson()
                .body(Map.of("saleId", saleId.toString()))
                .when()
                .post("/api/fiscal/returns/issue")
                .then()
                .statusCode(409)
                .body("detail", equalTo("Reembolso confirmado e obrigatorio para nota de devolucao"));
    }

    private UUID seedProduct(UUID tenantId) {
        return productRepository.save(new Product(
                null,
                null,
                tenantId,
                "Produto fiscal",
                "FISC-" + UUID.randomUUID(),
                null,
                null
        )).getId();
    }

    private UUID seedSale() {
        Product product = productRepository.save(new Product(
                null,
                null,
                TENANT_ID,
                "Produto fiscal",
                "FISC-" + UUID.randomUUID(),
                null,
                null
        ));
        Sale sale = new Sale();
        sale.setSessionToken(UUID.randomUUID().toString());
        sale.setCompanyId(companyId);
        sale.setState(new CompletedState());
        sale.setSubtotal(new BigDecimal("10.00"));
        sale.setTotal(new BigDecimal("10.00"));
        sale.setAmountDue(BigDecimal.ZERO);
        sale.getItems().add(new dev.kalles.core.entity.SaleItem(sale, product, 1, new BigDecimal("10.00")));
        return saleRepository.save(sale).getId();
    }

    private void seedRefund(UUID saleId) {
        Sale sale = saleRepository.findById(saleId).orElseThrow();
        paymentRepository.save(new dev.kalles.core.entity.Payment(
                sale,
                dev.kalles.core.enums.payment.PaymentMethod.CASH,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                "refund:" + saleId,
                true
        ));
    }

    private UUID seedDocument(UUID saleId, FiscalDocumentStatus status) {
        FiscalDocumentEntity entity = new FiscalDocumentEntity();
        entity.setTenantId(TENANT_ID);
        entity.setCompanyId(companyId);
        entity.setSaleId(saleId);
        entity.setModel(FiscalDocumentModel.NFCE);
        entity.setEnvironment(FiscalEnvironment.HOMOLOGACAO);
        entity.setStatus(status);
        entity.setAccessKey(status == FiscalDocumentStatus.AUTORIZADO ? "NFCe-HOM-" + saleId : null);
        entity.setAuthorizationProtocol(status == FiscalDocumentStatus.AUTORIZADO ? "HOM-135260000000001" : null);
        entity.setRejectionReason(status == FiscalDocumentStatus.REJEITADO ? "Rejeicao" : null);
        entity.setAuthorizedXml(status == FiscalDocumentStatus.AUTORIZADO
                ? "<NFe><infNFe Id=\"NFCe-HOM-" + saleId + "\"/></NFe>"
                : null);
        entity.setIssuedAt(Instant.now());
        return fiscalDocumentRepository.save(entity).getId();
    }

    private io.restassured.specification.RequestSpecification authenticatedJson() {
        return authenticated()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private Map<String, Object> validPreparationPayload(Instant certificateExpiresAt) {
        return Map.ofEntries(
                Map.entry("cnpj", "11.222.333/0001-81"),
                Map.entry("legalName", "Kalles Comercio LTDA"),
                Map.entry("tradeName", "Kalles Matriz"),
                Map.entry("stateRegistration", "110.042.490.114"),
                Map.entry("taxRegime", "SIMPLES_NACIONAL"),
                Map.entry("cnae", "4712100"),
                Map.entry("zipCode", "01001-000"),
                Map.entry("stateCode", "SP"),
                Map.entry("stateIbgeCode", 35),
                Map.entry("cityName", "Sao Paulo"),
                Map.entry("cityIbgeCode", 3550308),
                Map.entry("district", "Se"),
                Map.entry("street", "Praca da Se"),
                Map.entry("number", "100"),
                Map.entry("complement", "Loja 1"),
                Map.entry("countryName", "Brasil"),
                Map.entry("countryCode", 1058),
                Map.entry("model", "NFCE"),
                Map.entry("environment", "HOMOLOGACAO"),
                Map.entry("cscId", "1"),
                Map.entry("cscToken", "CSC-HOMOLOGACAO"),
                Map.entry("series", 1),
                Map.entry("nextNumber", 100),
                Map.entry("certificateBase64", "BASE64-PFX"),
                Map.entry("certificatePassword", "pfx-password"),
                Map.entry("certificateExpiresAt", certificateExpiresAt.toString())
        );
    }

    private io.restassured.specification.RequestSpecification authenticated() {
        return given()
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString());
    }

    private HttpResponse<String> httpGetString(String path) throws Exception {
        return HTTP_CLIENT.send(authenticatedGet(path).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<byte[]> httpGetBytes(String path) throws Exception {
        return HTTP_CLIENT.send(authenticatedGet(path).build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpRequest.Builder authenticatedGet(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .header("Cookie", "kalles_auth_token=" + auth.authCookie())
                .header("X-Company-ID", companyId.toString());
    }
}
