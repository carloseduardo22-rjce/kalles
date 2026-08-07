package dev.kalles.fiscal.integration;

import dev.kalles.cashregister.support.AbstractCashRegisterApiSupport;
import dev.kalles.core.entity.Company;
import dev.kalles.core.entity.Payment;
import dev.kalles.core.entity.Product;
import dev.kalles.core.entity.Sale;
import dev.kalles.core.enums.payment.PaymentMethod;
import dev.kalles.core.repository.PaymentRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.core.repository.SaleRepository;
import dev.kalles.core.state.CompletedState;
import dev.kalles.core.state.OpenState;
import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalCertificateEntity;
import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalConfigurationEntity;
import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalDocumentEntity;
import dev.kalles.fiscal.adapter.out.persistence.entity.FiscalProductClassificationEntity;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalCertificateRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalConfigurationRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalDocumentRepository;
import dev.kalles.fiscal.adapter.out.persistence.repository.SpringDataFiscalProductClassificationRepository;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.support.ControllableSefazAuthorizationPort;
import dev.kalles.fiscal.support.FiscalTestConfiguration;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("integration")
@Import(FiscalTestConfiguration.class)
class FiscalNfceApiIntegrationTest extends AbstractCashRegisterApiSupport {

    private static final UUID OTHER_TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174311");
    private static final String SEFAZ_REJECTION = "Rejeicao: total da NFC-e difere do somatorio dos itens";

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
    private ControllableSefazAuthorizationPort sefazAuthorizationPort;

    private AuthContext auth;

    @BeforeEach
    void setUp() {
        resetScenarioData();
        fiscalDocumentRepository.deleteAll();
        fiscalConfigurationRepository.deleteAll();
        fiscalCertificateRepository.deleteAll();
        fiscalProductClassificationRepository.deleteAll();
        paymentRepository.deleteAll();
        auth = authenticateOperator();
        sefazAuthorizationPort.reset();
    }

    @Test
    void shouldIssueNfceForCompletedPaidSaleInActiveTenantAndCompany() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID saleId = seedSale(companyId, true, true);

        issueNfce(saleId)
                .statusCode(201)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("saleId", equalTo(saleId.toString()))
                .body("model", equalTo("NFCE"))
                .body("environment", equalTo("HOMOLOGACAO"))
                .body("status", equalTo("AUTORIZADO"))
                .body("accessKey", notNullValue())
                .body("authorizationProtocol", notNullValue());
    }

    @Test
    void shouldReturnConflictWhenSaleIsNotPaid() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID saleId = seedSale(companyId, false, true);

        issueNfce(saleId)
                .statusCode(409)
                .body("detail", equalTo("A NFC-e so pode ser emitida para venda finalizada e paga"));
    }

    @Test
    void shouldReturnConflictWhenSaleAlreadyHasAuthorizedFiscalDocument() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID saleId = seedSale(companyId, true, true);
        seedAuthorizedDocument(saleId);

        issueNfce(saleId)
                .statusCode(409)
                .body("detail", equalTo("A venda ja possui documento fiscal autorizado"));
    }

    @Test
    void shouldReturnConflictWhenCompanyHasNoFiscalConfiguration() {
        seedValidCertificate();
        UUID saleId = seedSale(companyId, true, true);

        issueNfce(saleId)
                .statusCode(409)
                .body("detail", equalTo("Configuracao fiscal da filial nao encontrada"));
    }

    @Test
    void shouldReturnConflictWhenCompanyHasNoValidCertificate() {
        seedFiscalConfiguration();
        UUID saleId = seedSale(companyId, true, true);

        issueNfce(saleId)
                .statusCode(409)
                .body("detail", equalTo("Certificado digital valido e obrigatorio para emissao fiscal"));
    }

    @Test
    void shouldReturnBadRequestWhenSaleItemHasNoMinimumFiscalClassification() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID saleId = seedSale(companyId, true, false);

        issueNfce(saleId)
                .statusCode(400)
                .body("detail", equalTo("Todos os itens da NFC-e devem possuir classificacao fiscal minima"));
    }

    @Test
    void shouldPersistRejectedFiscalDocumentWhenSefazRejectsNfce() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID saleId = seedSale(companyId, true, true);
        sefazAuthorizationPort.rejectNext(SEFAZ_REJECTION);

        issueNfce(saleId)
                .statusCode(422)
                .body("tenantId", equalTo(TENANT_ID.toString()))
                .body("companyId", equalTo(companyId.toString()))
                .body("saleId", equalTo(saleId.toString()))
                .body("status", equalTo("REJEITADO"))
                .body("rejectionReason", equalTo(SEFAZ_REJECTION))
                .body("authorizationProtocol", equalTo(null));
    }

    @Test
    void shouldReturnNotFoundWhenSaleBelongsToAnotherTenant() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID foreignCompanyId = seedForeignTenantCompany();
        UUID saleId = seedSale(foreignCompanyId, true, true);

        issueNfce(saleId)
                .statusCode(404);
    }

    @Test
    void shouldReturnNotFoundWhenSaleBelongsToAnotherCompanyFromSameTenant() {
        seedFiscalConfiguration();
        seedValidCertificate();
        UUID foreignCompanyId = seedCompany("Filial fiscal secundaria");
        UUID saleId = seedSale(foreignCompanyId, true, true);

        issueNfce(saleId)
                .statusCode(404);
    }

    private UUID seedSale(UUID targetCompanyId, boolean completed, boolean withNcm) {
        Product product = productRepository.save(new Product(
                null,
                null,
                TENANT_ID,
                "Produto fiscal",
                "FISC-" + UUID.randomUUID(),
                null,
                null
        ));

        if (withNcm) {
            seedProductClassification(targetCompanyId, product.getId());
        }

        Sale sale = new Sale();
        sale.setSessionToken(UUID.randomUUID().toString());
        sale.setCompanyId(targetCompanyId);
        sale.setState(completed ? new CompletedState() : new OpenState());
        sale.setSubtotal(new BigDecimal("10.00"));
        sale.setTotal(new BigDecimal("10.00"));
        sale.setAmountDue(completed ? BigDecimal.ZERO : new BigDecimal("10.00"));
        sale.getItems().add(new dev.kalles.core.entity.SaleItem(sale, product, 1, new BigDecimal("10.00")));
        if (completed) {
            sale.getPayments().add(new Payment(sale, PaymentMethod.CASH, new BigDecimal("10.00"), BigDecimal.ZERO, "cash-fiscal", true));
        }
        return saleRepository.save(sale).getId();
    }

    private void seedProductClassification(UUID targetCompanyId, UUID productId) {
        FiscalProductClassificationEntity entity = new FiscalProductClassificationEntity();
        entity.setTenantId(TENANT_ID);
        entity.setCompanyId(targetCompanyId);
        entity.setProductId(productId);
        entity.setNcm("61091000");
        entity.setCfop("5102");
        fiscalProductClassificationRepository.save(entity);
    }

    private void seedFiscalConfiguration() {
        FiscalConfigurationEntity entity = new FiscalConfigurationEntity();
        entity.setTenantId(TENANT_ID);
        entity.setCompanyId(companyId);
        entity.setModel(FiscalDocumentModel.NFCE);
        entity.setEnvironment(FiscalEnvironment.HOMOLOGACAO);
        entity.setStateCode("SP");
        entity.setCscId("1");
        entity.setCscToken("CSC-HOMOLOGACAO");
        entity.setSeries(1);
        entity.setNextNumber(100L);
        fiscalConfigurationRepository.save(entity);
    }

    private void seedValidCertificate() {
        FiscalCertificateEntity entity = new FiscalCertificateEntity();
        entity.setTenantId(TENANT_ID);
        entity.setCompanyId(companyId);
        entity.setActive(true);
        entity.setExpiresAt(Instant.now().plusSeconds(3600));
        entity.setProtectedContent("protected-certificate-test");
        entity.setProtectedPassword("protected-password-test");
        fiscalCertificateRepository.save(entity);
    }

    private void seedAuthorizedDocument(UUID saleId) {
        FiscalDocumentEntity entity = new FiscalDocumentEntity();
        entity.setTenantId(TENANT_ID);
        entity.setCompanyId(companyId);
        entity.setSaleId(saleId);
        entity.setModel(FiscalDocumentModel.NFCE);
        entity.setEnvironment(FiscalEnvironment.HOMOLOGACAO);
        entity.setStatus(FiscalDocumentStatus.AUTORIZADO);
        entity.setAccessKey("NFCe-HOM-" + saleId);
        entity.setAuthorizationProtocol("HOM-135260000000001");
        entity.setAuthorizedXml("<NFe><infNFe Id=\"NFCe-HOM-" + saleId + "\"/></NFe>");
        entity.setIssuedAt(Instant.now());
        fiscalDocumentRepository.save(entity);
    }

    private UUID seedForeignTenantCompany() {
        tenantRepository.save(new dev.kalles.core.entity.Tenant(OTHER_TENANT_ID, "Tenant Fiscal Externo"));
        return companyRepository.save(new Company(
                null,
                "Loja fiscal externa",
                OTHER_TENANT_ID,
                null,
                null,
                null,
                null,
                null,
                null
        )).getId();
    }

    private ValidatableResponse issueNfce(UUID saleId) {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "saleId", saleId.toString(),
                        "model", "NFCE",
                        "environment", "HOMOLOGACAO"
                ))
                .when()
                .post("/api/fiscal/nfce/issue")
                .then();
    }
}
