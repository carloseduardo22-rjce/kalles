package dev.kalles.fiscal.steps;

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
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class FiscalNfceStepDefinitions extends AbstractCashRegisterApiSupport {

    private static final UUID OTHER_TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174311");

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
    private UUID currentSaleId;
    private Response response;

    @Before("@fiscal")
    public void resetFiscalScenario() {
        resetScenarioData();
        fiscalDocumentRepository.deleteAll();
        fiscalConfigurationRepository.deleteAll();
        fiscalCertificateRepository.deleteAll();
        fiscalProductClassificationRepository.deleteAll();
        paymentRepository.deleteAll();
        auth = authenticateOperator();
        sefazAuthorizationPort.reset();
        currentSaleId = null;
        response = null;
    }

    @Dado("que o tenant atual possui uma empresa fiscal configurada para NFC-e em homologacao")
    public void tenantAtualPossuiEmpresaFiscalConfigurada() {
        seedFiscalConfiguration();
    }

    @E("a filial ativa possui certificado digital valido")
    public void filialAtivaPossuiCertificadoValido() {
        seedValidCertificate();
    }

    @E("a venda do PDV pertence ao tenant e filial ativos")
    public void vendaPertenceAoTenantEFilialAtivos() {
        assertThat(companyId).isNotNull();
    }

    @Dado("que existe uma venda finalizada e paga com itens fiscais validos")
    @E("existe uma venda finalizada e paga com itens fiscais validos")
    public void existeVendaFinalizadaPagaComItensFiscaisValidos() {
        currentSaleId = seedSale(companyId, true, true);
    }

    @E("a venda ainda nao possui documento fiscal autorizado")
    public void vendaAindaNaoPossuiDocumentoAutorizado() {
        assertThat(fiscalDocumentRepository.findAll()).isEmpty();
    }

    @Quando("o usuario solicitar a emissao da NFC-e para a venda")
    public void usuarioSolicitarEmissaoDaNfce() {
        response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie("kalles_auth_token", auth.authCookie())
                .cookie("XSRF-TOKEN", auth.csrfCookie())
                .header("X-XSRF-TOKEN", auth.csrfToken())
                .header("X-Company-ID", companyId.toString())
                .body(Map.of(
                        "saleId", currentSaleId.toString(),
                        "model", "NFCE",
                        "environment", "HOMOLOGACAO"
                ))
                .when()
                .post("/api/fiscal/nfce/issue");
    }

    @Entao("a resposta fiscal deve ter status HTTP {int}")
    public void respostaFiscalDeveTerStatusHttp(int status) {
        response.then().statusCode(status);
    }

    @E("o documento fiscal deve ser criado para o tenant atual")
    public void documentoFiscalCriadoParaTenantAtual() {
        response.then().body("tenantId", equalTo(TENANT_ID.toString()));
    }

    @E("o documento fiscal deve ser vinculado a filial ativa")
    public void documentoFiscalVinculadoAFilialAtiva() {
        response.then().body("companyId", equalTo(companyId.toString()));
    }

    @E("o documento fiscal deve registrar o modelo {string}")
    public void documentoFiscalDeveRegistrarModelo(String model) {
        response.then().body("model", equalTo(model));
    }

    @E("o documento fiscal deve registrar o ambiente {string}")
    public void documentoFiscalDeveRegistrarAmbiente(String environment) {
        response.then().body("environment", equalTo(environment));
    }

    @E("o documento fiscal deve registrar status {string}")
    public void documentoFiscalDeveRegistrarStatus(String status) {
        response.then().body("status", equalTo(status));
    }

    @E("o documento fiscal deve armazenar a chave de acesso retornada pela SEFAZ")
    public void documentoFiscalDeveArmazenarChaveDeAcesso() {
        response.then().body("accessKey", notNullValue());
    }

    @E("o documento fiscal deve armazenar o numero de protocolo retornado pela SEFAZ")
    public void documentoFiscalDeveArmazenarProtocolo() {
        response.then().body("authorizationProtocol", notNullValue());
    }

    @E("a resposta fiscal deve informar {string}")
    public void respostaFiscalDeveInformar(String detail) {
        response.then().body("detail", equalTo(detail));
    }

    @Dado("que existe uma venda aberta ou pendente de pagamento")
    public void existeVendaAbertaOuPendentePagamento() {
        currentSaleId = seedSale(companyId, false, true);
    }

    @Dado("que existe uma venda finalizada e paga com documento fiscal autorizado")
    public void existeVendaComDocumentoAutorizado() {
        currentSaleId = seedSale(companyId, true, true);
        seedAuthorizedDocument(currentSaleId);
    }

    @Dado("que a filial ativa nao possui configuracao fiscal para NFC-e")
    public void filialAtivaNaoPossuiConfiguracaoFiscal() {
        fiscalConfigurationRepository.deleteAll();
    }

    @Dado("que a filial ativa nao possui certificado digital valido")
    public void filialAtivaNaoPossuiCertificadoValido() {
        fiscalCertificateRepository.deleteAll();
    }

    @Dado("que existe uma venda finalizada e paga com item sem NCM")
    public void existeVendaComItemSemNcm() {
        currentSaleId = seedSale(companyId, true, false);
    }

    @E("a SEFAZ retorna rejeicao {string}")
    public void sefazRetornaRejeicao(String rejeicao) {
        sefazAuthorizationPort.rejectNext(rejeicao);
    }

    @E("o documento fiscal deve armazenar o motivo da rejeicao da SEFAZ")
    public void documentoFiscalArmazenaMotivoRejeicao() {
        response.then().body("rejectionReason", notNullValue());
    }

    @E("o documento fiscal nao deve armazenar protocolo de autorizacao")
    public void documentoFiscalNaoArmazenaProtocolo() {
        response.then().body("authorizationProtocol", equalTo(null));
    }

    @Dado("que existe uma venda finalizada e paga em outro tenant")
    public void existeVendaEmOutroTenant() {
        UUID foreignCompanyId = seedForeignTenantCompany();
        currentSaleId = seedSale(foreignCompanyId, true, true);
    }

    @Dado("que existe uma venda finalizada e paga em outra filial do mesmo tenant")
    public void existeVendaEmOutraFilialMesmoTenant() {
        UUID foreignCompanyId = seedCompany("Filial fiscal secundaria");
        currentSaleId = seedSale(foreignCompanyId, true, true);
    }

    @Quando("o usuario do tenant atual solicitar a emissao da NFC-e para essa venda")
    public void usuarioTenantAtualSolicitarEmissaoDessaVenda() {
        usuarioSolicitarEmissaoDaNfce();
    }

    @Quando("o usuario solicitar a emissao da NFC-e informando a filial ativa atual")
    public void usuarioSolicitarEmissaoInformandoFilialAtivaAtual() {
        usuarioSolicitarEmissaoDaNfce();
    }

    @E("nenhum documento fiscal deve ser criado")
    @E("nenhum documento fiscal deve ser criado no tenant atual")
    @E("nenhum documento fiscal deve ser criado no outro tenant")
    @E("nenhum documento fiscal deve ser criado para a filial ativa atual")
    @E("nenhum documento fiscal deve ser criado para a outra filial")
    public void nenhumDocumentoFiscalDeveSerCriado() {
        assertThat(fiscalDocumentRepository.findAll())
                .noneMatch(document -> FiscalDocumentStatus.AUTORIZADO.equals(document.getStatus()));
    }

    @E("nenhum novo documento fiscal deve ser criado")
    public void nenhumNovoDocumentoFiscalDeveSerCriado() {
        assertThat(fiscalDocumentRepository.findAll())
                .filteredOn(document -> FiscalDocumentStatus.AUTORIZADO.equals(document.getStatus()))
                .hasSize(1);
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
}
