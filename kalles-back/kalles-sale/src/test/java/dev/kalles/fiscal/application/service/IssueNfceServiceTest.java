package dev.kalles.fiscal.application.service;

import dev.kalles.core.exception.NotFoundException;
import dev.kalles.fiscal.application.port.in.IssueNfceCommand;
import dev.kalles.fiscal.application.port.out.FiscalCertificateRepository;
import dev.kalles.fiscal.application.port.out.FiscalConfigurationRepository;
import dev.kalles.fiscal.application.port.out.FiscalDocumentRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerAddressRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerProfileRepository;
import dev.kalles.fiscal.application.port.out.FiscalSaleReader;
import dev.kalles.fiscal.application.port.out.SefazAuthorizationPort;
import dev.kalles.fiscal.domain.FiscalCertificate;
import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.domain.FiscalIssuerAddress;
import dev.kalles.fiscal.domain.FiscalIssuerProfile;
import dev.kalles.fiscal.domain.FiscalSale;
import dev.kalles.fiscal.domain.FiscalSaleItem;
import dev.kalles.fiscal.domain.FiscalSaleStatus;
import dev.kalles.fiscal.domain.FiscalTaxRegime;
import dev.kalles.fiscal.domain.SefazAuthorizationResult;
import dev.kalles.fiscal.exception.FiscalConflictException;
import dev.kalles.fiscal.exception.FiscalRejectionException;
import dev.kalles.fiscal.exception.FiscalValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class IssueNfceServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");
    private static final UUID COMPANY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174112");
    private static final UUID SALE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174113");
    private static final Instant NOW = Instant.parse("2026-04-30T12:00:00Z");

    @Mock
    private FiscalSaleReader saleReader;

    @Mock
    private FiscalConfigurationRepository configurationRepository;

    @Mock
    private FiscalCertificateRepository certificateRepository;

    @Mock
    private FiscalDocumentRepository documentRepository;

    @Mock
    private FiscalIssuerProfileRepository issuerProfileRepository;

    @Mock
    private FiscalIssuerAddressRepository issuerAddressRepository;

    @Mock
    private SefazAuthorizationPort sefazAuthorizationPort;

    private IssueNfceService service;

    @BeforeEach
    void setUp() {
        service = new IssueNfceService(
                saleReader,
                configurationRepository,
                certificateRepository,
                documentRepository,
                issuerProfileRepository,
                issuerAddressRepository,
                sefazAuthorizationPort,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldIssueAuthorizedNfceForPaidSale() {
        IssueNfceCommand command = command();
        FiscalSale sale = paidSale("61091000");
        FiscalConfiguration configuration = configuration();
        FiscalCertificate certificate = certificate();

        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(sale));
        when(documentRepository.existsBySaleAndStatus(TENANT_ID, COMPANY_ID, SALE_ID,
                FiscalDocumentModel.NFCE, FiscalDocumentStatus.AUTORIZADO)).thenReturn(false);
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO)).thenReturn(Optional.of(configuration));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(certificate));
        when(sefazAuthorizationPort.authorize(sale, configuration, certificate))
                .thenReturn(SefazAuthorizationResult.authorized("35260412345678000123650010000000011000000018", "135260000000001"));
        when(documentRepository.save(any(FiscalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalDocument document = service.issue(command);

        assertThat(document.status()).isEqualTo(FiscalDocumentStatus.AUTORIZADO);
        assertThat(document.accessKey()).isEqualTo("35260412345678000123650010000000011000000018");
        assertThat(document.authorizationProtocol()).isEqualTo("135260000000001");
        assertThat(document.tenantId()).isEqualTo(TENANT_ID);
        assertThat(document.companyId()).isEqualTo(COMPANY_ID);
    }

    @Test
    void shouldReturnNotFoundWhenSaleIsOutsideTenantOrCompanyScope() {
        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Venda nao encontrada");
    }

    @Test
    void shouldRejectSaleThatIsNotPaidOrCompleted() {
        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID))
                .thenReturn(Optional.of(new FiscalSale(SALE_ID, TENANT_ID, COMPANY_ID,
                        FiscalSaleStatus.OPEN, BigDecimal.TEN, List.of(item("61091000")))));

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("A NFC-e so pode ser emitida para venda finalizada e paga");
    }

    @Test
    void shouldRejectWhenAuthorizedFiscalDocumentAlreadyExists() {
        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(paidSale("61091000")));
        when(documentRepository.existsBySaleAndStatus(TENANT_ID, COMPANY_ID, SALE_ID,
                FiscalDocumentModel.NFCE, FiscalDocumentStatus.AUTORIZADO)).thenReturn(true);

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("A venda ja possui documento fiscal autorizado");
    }

    @Test
    void shouldRejectWhenFiscalConfigurationIsMissing() {
        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(paidSale("61091000")));
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("Configuracao fiscal da filial nao encontrada");
    }

    @Test
    void shouldRejectWhenCertificateIsMissingOrExpired() {
        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(paidSale("61091000")));
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO)).thenReturn(Optional.of(configuration()));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID))
                .thenReturn(Optional.of(new FiscalCertificate(UUID.randomUUID(), TENANT_ID, COMPANY_ID,
                        NOW.minusSeconds(1), true, "protected-content", "protected-password")));

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("Certificado digital valido e obrigatorio para emissao fiscal");
    }

    @Test
    void shouldRejectWhenAnyItemHasNoNcm() {
        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(paidSale(null)));
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO)).thenReturn(Optional.of(configuration()));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(certificate()));

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("Todos os itens da NFC-e devem possuir classificacao fiscal minima");

        verify(sefazAuthorizationPort, never()).authorize(any(), any(), any());
    }

    @Test
    void shouldRejectProductionIssueWhenIssuerReadinessIsMissing() {
        IssueNfceCommand command = productionCommand();

        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(paidSale("61091000")));
        when(documentRepository.existsBySaleAndStatus(TENANT_ID, COMPANY_ID, SALE_ID,
                FiscalDocumentModel.NFCE, FiscalDocumentStatus.AUTORIZADO)).thenReturn(false);
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.PRODUCAO)).thenReturn(Optional.of(productionConfiguration()));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(certificate()));
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(issuerAddressRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(issuerAddress()));

        assertThatThrownBy(() -> service.issue(command))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("Filial nao esta pronta para emissao fiscal");

        verify(sefazAuthorizationPort, never()).authorize(any(), any(), any());
    }

    @Test
    void shouldAllowProductionIssueWhenIssuerReadinessIsComplete() {
        IssueNfceCommand command = productionCommand();
        FiscalSale sale = paidSale("61091000");
        FiscalConfiguration configuration = productionConfiguration();
        FiscalCertificate certificate = certificate();

        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(sale));
        when(documentRepository.existsBySaleAndStatus(TENANT_ID, COMPANY_ID, SALE_ID,
                FiscalDocumentModel.NFCE, FiscalDocumentStatus.AUTORIZADO)).thenReturn(false);
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.PRODUCAO)).thenReturn(Optional.of(configuration));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(certificate));
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(issuerProfile()));
        when(issuerAddressRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(issuerAddress()));
        when(sefazAuthorizationPort.authorize(sale, configuration, certificate))
                .thenReturn(SefazAuthorizationResult.authorized("35260412345678000123650010000000011000000018", "135260000000001"));
        when(documentRepository.save(any(FiscalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalDocument document = service.issue(command);

        assertThat(document.environment()).isEqualTo(FiscalEnvironment.PRODUCAO);
        assertThat(document.status()).isEqualTo(FiscalDocumentStatus.AUTORIZADO);
    }

    @Test
    void shouldPersistRejectedDocumentAndExposeRejection() {
        FiscalSale sale = paidSale("61091000");
        FiscalConfiguration configuration = configuration();
        FiscalCertificate certificate = certificate();

        when(saleReader.findByIdForTenantAndCompany(SALE_ID, TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(sale));
        when(configurationRepository.findByCompany(TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO)).thenReturn(Optional.of(configuration));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(certificate));
        when(sefazAuthorizationPort.authorize(sale, configuration, certificate))
                .thenReturn(SefazAuthorizationResult.rejected("Rejeicao: total da NFC-e difere do somatorio dos itens"));
        when(documentRepository.save(any(FiscalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> service.issue(command()))
                .isInstanceOf(FiscalRejectionException.class)
                .hasMessage("Rejeicao: total da NFC-e difere do somatorio dos itens");
    }

    private IssueNfceCommand command() {
        return new IssueNfceCommand(TENANT_ID, COMPANY_ID, SALE_ID, FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO);
    }

    private IssueNfceCommand productionCommand() {
        return new IssueNfceCommand(TENANT_ID, COMPANY_ID, SALE_ID, FiscalDocumentModel.NFCE, FiscalEnvironment.PRODUCAO);
    }

    private FiscalSale paidSale(String ncm) {
        return new FiscalSale(SALE_ID, TENANT_ID, COMPANY_ID, FiscalSaleStatus.COMPLETED, BigDecimal.TEN, List.of(item(ncm)));
    }

    private FiscalSaleItem item(String ncm) {
        return new FiscalSaleItem(UUID.randomUUID(), "Produto teste", ncm, 1, BigDecimal.TEN, BigDecimal.TEN);
    }

    private FiscalConfiguration configuration() {
        return new FiscalConfiguration(UUID.randomUUID(), TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO, "SP", "1", "CSC", 1, 100L);
    }

    private FiscalConfiguration productionConfiguration() {
        return new FiscalConfiguration(UUID.randomUUID(), TENANT_ID, COMPANY_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.PRODUCAO, "SP", "1", "CSC", 1, 100L);
    }

    private FiscalCertificate certificate() {
        return new FiscalCertificate(UUID.randomUUID(), TENANT_ID, COMPANY_ID,
                NOW.plusSeconds(3600), true, "protected-content", "protected-password");
    }

    private FiscalIssuerProfile issuerProfile() {
        return new FiscalIssuerProfile(UUID.randomUUID(), TENANT_ID, COMPANY_ID, "11222333000181",
                "Kalles Comercio LTDA", "Kalles Matriz", "110042490114",
                FiscalTaxRegime.SIMPLES_NACIONAL, "4712100");
    }

    private FiscalIssuerAddress issuerAddress() {
        return new FiscalIssuerAddress(UUID.randomUUID(), TENANT_ID, COMPANY_ID, "01001000", "SP",
                35, "Sao Paulo", 3550308, "Se", "Praca da Se", "100", null, "Brasil", 1058);
    }
}
