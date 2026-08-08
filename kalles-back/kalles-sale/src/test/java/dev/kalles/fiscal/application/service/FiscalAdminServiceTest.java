package dev.kalles.fiscal.application.service;

import dev.kalles.fiscal.application.port.in.RegisterFiscalCertificateCommand;
import dev.kalles.fiscal.application.port.in.SaveFiscalConfigurationCommand;
import dev.kalles.fiscal.application.port.in.SaveFiscalIssuerAddressCommand;
import dev.kalles.fiscal.application.port.in.SaveFiscalIssuerProfileCommand;
import dev.kalles.fiscal.application.port.in.SaveFiscalPreparationCommand;
import dev.kalles.fiscal.application.port.in.SaveFiscalProductClassificationCommand;
import dev.kalles.fiscal.application.port.out.FiscalCertificateRepository;
import dev.kalles.fiscal.application.port.out.FiscalCompanyAccessPort;
import dev.kalles.fiscal.application.port.out.FiscalConfigurationRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerAddressRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerProfileRepository;
import dev.kalles.fiscal.application.port.out.FiscalProductAccessPort;
import dev.kalles.fiscal.application.port.out.FiscalProductClassificationRepository;
import dev.kalles.fiscal.domain.FiscalCertificate;
import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.domain.FiscalIssuerAddress;
import dev.kalles.fiscal.domain.FiscalIssuerProfile;
import dev.kalles.fiscal.domain.FiscalProductClassification;
import dev.kalles.fiscal.domain.FiscalReadiness;
import dev.kalles.fiscal.domain.FiscalTaxRegime;
import dev.kalles.fiscal.exception.FiscalValidationException;
import dev.kalles.note.application.port.out.CryptoPort;
import dev.kalles.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class FiscalAdminServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");
    private static final UUID COMPANY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174112");
    private static final UUID PRODUCT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174113");
    private static final Instant NOW = Instant.parse("2026-04-30T12:00:00Z");

    @Mock
    private FiscalCompanyAccessPort companyAccessPort;

    @Mock
    private FiscalProductAccessPort productAccessPort;

    @Mock
    private FiscalConfigurationRepository configurationRepository;

    @Mock
    private FiscalCertificateRepository certificateRepository;

    @Mock
    private FiscalProductClassificationRepository classificationRepository;

    @Mock
    private FiscalIssuerProfileRepository issuerProfileRepository;

    @Mock
    private FiscalIssuerAddressRepository issuerAddressRepository;

    @Mock
    private CryptoPort cryptoPort;

    private FiscalAdminService service;

    @BeforeEach
    void setUp() {
        service = new FiscalAdminService(
                companyAccessPort,
                productAccessPort,
                configurationRepository,
                certificateRepository,
                classificationRepository,
                issuerProfileRepository,
                issuerAddressRepository,
                cryptoPort,
                "secret",
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldSaveIssuerProfileWithNormalizedFiscalData() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(issuerProfileRepository.save(any(FiscalIssuerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalIssuerProfile saved = service.saveIssuerProfile(new SaveFiscalIssuerProfileCommand(
                TENANT_ID,
                COMPANY_ID,
                "11.222.333/0001-81",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "110.042.490.114",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "47.12-1-00"
        ));

        assertThat(saved.cnpj()).isEqualTo("11222333000181");
        assertThat(saved.stateRegistration()).isEqualTo("110042490114");
        assertThat(saved.cnae()).isEqualTo("4712100");
        assertThat(saved.taxRegime()).isEqualTo(FiscalTaxRegime.SIMPLES_NACIONAL);
    }

    @Test
    void shouldRejectInvalidIssuerCnpj() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.saveIssuerProfile(new SaveFiscalIssuerProfileCommand(
                TENANT_ID,
                COMPANY_ID,
                "11.111.111/1111-11",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "110042490114",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "4712100"
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("CNPJ do emissor fiscal invalido");
    }

    @Test
    void shouldRequireIssuerStateRegistrationForNfce() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.saveIssuerProfile(new SaveFiscalIssuerProfileCommand(
                TENANT_ID,
                COMPANY_ID,
                "11.222.333/0001-81",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "4712100"
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("Inscricao estadual e obrigatoria para NFC-e");
    }

    @Test
    void shouldSaveIssuerAddressWithBrazilAsDefaultCountry() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(issuerAddressRepository.save(any(FiscalIssuerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalIssuerAddress saved = service.saveIssuerAddress(new SaveFiscalIssuerAddressCommand(
                TENANT_ID,
                COMPANY_ID,
                "01001-000",
                "SP",
                35,
                "Sao Paulo",
                3550308,
                "Se",
                "Praca da Se",
                "100",
                "Loja 1",
                null,
                null
        ));

        assertThat(saved.zipCode()).isEqualTo("01001000");
        assertThat(saved.countryName()).isEqualTo("Brasil");
        assertThat(saved.countryCode()).isEqualTo(1058);
    }

    @Test
    void shouldSaveFiscalPreparationAndReturnReadyStatus() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(cryptoPort.encrypt("BASE64-PFX", "secret")).thenReturn("protected-content");
        when(cryptoPort.encrypt("pfx-password", "secret")).thenReturn("protected-password");
        when(issuerProfileRepository.save(any(FiscalIssuerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issuerAddressRepository.save(any(FiscalIssuerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(configurationRepository.save(any(FiscalConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(certificateRepository.save(any(FiscalCertificate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(issuerProfile()));
        when(issuerAddressRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(issuerAddress()));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(validCertificate()));

        FiscalReadiness readiness = service.savePreparation(validPreparationCommand());

        assertThat(readiness.ready()).isTrue();
        assertThat(readiness.missingItems()).isEmpty();
        verify(certificateRepository).deactivateActiveByCompany(TENANT_ID, COMPANY_ID);
        verify(certificateRepository).save(any(FiscalCertificate.class));
    }

    @Test
    void shouldRejectFiscalPreparationWithInvalidCnpjBeforePersistingSensitiveData() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.savePreparation(new SaveFiscalPreparationCommand(
                TENANT_ID, COMPANY_ID, "11.111.111/1111-11", "Kalles Comercio LTDA", "Kalles Matriz",
                "110042490114", FiscalTaxRegime.SIMPLES_NACIONAL, "4712100",
                "01001000", "SP", 35, "Sao Paulo", 3550308, "Se", "Praca da Se", "100",
                null, "Brasil", 1058, FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO,
                "1", "CSC-HOMOLOGACAO", 1, 100L, "BASE64-PFX", "pfx-password", NOW.plusSeconds(3600)
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("CNPJ do emissor fiscal invalido");

        verify(certificateRepository, never()).deactivateActiveByCompany(TENANT_ID, COMPANY_ID);
        verify(certificateRepository, never()).save(any(FiscalCertificate.class));
    }

    @Test
    void shouldRejectFiscalPreparationWithExpiredCertificateBeforeSavingCertificate() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.savePreparation(new SaveFiscalPreparationCommand(
                TENANT_ID, COMPANY_ID, "11.222.333/0001-81", "Kalles Comercio LTDA", "Kalles Matriz",
                "110042490114", FiscalTaxRegime.SIMPLES_NACIONAL, "4712100",
                "01001000", "SP", 35, "Sao Paulo", 3550308, "Se", "Praca da Se", "100",
                null, "Brasil", 1058, FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO,
                "1", "CSC-HOMOLOGACAO", 1, 100L, "BASE64-PFX", "pfx-password", NOW.minusSeconds(1)
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("Certificado digital expirado");

        verify(certificateRepository, never()).deactivateActiveByCompany(TENANT_ID, COMPANY_ID);
        verify(certificateRepository, never()).save(any(FiscalCertificate.class));
    }

    @Test
    void shouldSaveFiscalConfigurationInsideTenantAndCompanyScope() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(configurationRepository.save(any(FiscalConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalConfiguration saved = service.saveConfiguration(new SaveFiscalConfigurationCommand(
                TENANT_ID, COMPANY_ID, FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO,
                "SP", "1", "CSC", 1, 100L
        ));

        assertThat(saved.tenantId()).isEqualTo(TENANT_ID);
        assertThat(saved.companyId()).isEqualTo(COMPANY_ID);
        assertThat(saved.series()).isEqualTo(1);
        assertThat(saved.nextNumber()).isEqualTo(100L);
    }

    @Test
    void shouldRejectInvalidFiscalNumbering() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.saveConfiguration(new SaveFiscalConfigurationCommand(
                TENANT_ID, COMPANY_ID, FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO,
                "SP", "1", "CSC", 0, 100L
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("Serie fiscal deve ser positiva");
    }

    @Test
    void shouldEncryptAndRotateActiveCertificate() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(cryptoPort.encrypt("BASE64-PFX", "secret")).thenReturn("protected-content");
        when(cryptoPort.encrypt("pfx-password", "secret")).thenReturn("protected-password");
        when(certificateRepository.save(any(FiscalCertificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalCertificate saved = service.registerCertificate(new RegisterFiscalCertificateCommand(
                TENANT_ID, COMPANY_ID, "BASE64-PFX", "pfx-password", NOW.plusSeconds(3600)
        ));

        verify(certificateRepository).deactivateActiveByCompany(TENANT_ID, COMPANY_ID);
        assertThat(saved.protectedContent()).isEqualTo("protected-content");
        assertThat(saved.protectedPassword()).isEqualTo("protected-password");
    }

    @Test
    void shouldRejectExpiredCertificate() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.registerCertificate(new RegisterFiscalCertificateCommand(
                TENANT_ID, COMPANY_ID, "BASE64-PFX", "pfx-password", NOW.minusSeconds(1)
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("Certificado digital expirado");
    }

    @Test
    void shouldSaveProductClassificationWhenProductBelongsToTenant() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(productAccessPort.existsProductByTenant(TENANT_ID, PRODUCT_ID)).thenReturn(true);
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(classificationRepository.save(any(FiscalProductClassification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalProductClassification saved = service.saveProductClassification(new SaveFiscalProductClassificationCommand(
                TENANT_ID, COMPANY_ID, PRODUCT_ID, "61091000", null, "5102"
        ));

        ArgumentCaptor<FiscalProductClassification> captor = ArgumentCaptor.forClass(FiscalProductClassification.class);
        verify(classificationRepository).save(captor.capture());
        assertThat(saved.productId()).isEqualTo(PRODUCT_ID);
        assertThat(captor.getValue().tenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void shouldRejectProductTaxationIncompatibleWithIssuerTaxRegime() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(productAccessPort.existsProductByTenant(TENANT_ID, PRODUCT_ID)).thenReturn(true);
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(new FiscalIssuerProfile(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                "11222333000181",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "110042490114",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "4712100"
        )));

        assertThatThrownBy(() -> service.saveProductClassification(new SaveFiscalProductClassificationCommand(
                TENANT_ID, COMPANY_ID, PRODUCT_ID, "61091000", "2805800", "5102",
                "5102", "0", null, "00", "UN", "7890000000000"
        )))
                .isInstanceOf(FiscalValidationException.class)
                .hasMessage("Tributacao do produto incompativel com o regime fiscal da filial");
    }

    @Test
    void shouldReportFiscalReadinessMissingItems() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(issuerAddressRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.empty());
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.empty());

        FiscalReadiness readiness = service.getReadiness(TENANT_ID, COMPANY_ID);

        assertThat(readiness.ready()).isFalse();
        assertThat(readiness.missingItems()).containsExactly(
                "Dados fiscais da filial",
                "Endereco fiscal da filial",
                "Certificado A1 valido"
        );
    }

    @Test
    void shouldReportFiscalReadinessWhenIssuerAddressAndCertificateAreValid() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(issuerProfileRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(new FiscalIssuerProfile(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                "11222333000181",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "110042490114",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "4712100"
        )));
        when(issuerAddressRepository.findByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(new FiscalIssuerAddress(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                "01001000",
                "SP",
                35,
                "Sao Paulo",
                3550308,
                "Se",
                "Praca da Se",
                "100",
                null,
                "Brasil",
                1058
        )));
        when(certificateRepository.findActiveByCompany(TENANT_ID, COMPANY_ID)).thenReturn(Optional.of(new FiscalCertificate(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                NOW.plusSeconds(3600),
                true,
                "protected-content",
                "protected-password"
        )));

        FiscalReadiness readiness = service.getReadiness(TENANT_ID, COMPANY_ID);

        assertThat(readiness.ready()).isTrue();
        assertThat(readiness.missingItems()).isEmpty();
    }

    @Test
    void shouldRejectProductFromAnotherTenant() {
        when(companyAccessPort.existsByTenant(TENANT_ID, COMPANY_ID)).thenReturn(true);
        when(productAccessPort.existsProductByTenant(TENANT_ID, PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.saveProductClassification(new SaveFiscalProductClassificationCommand(
                TENANT_ID, COMPANY_ID, PRODUCT_ID, "61091000", null, "5102"
        )))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Produto nao encontrado");
    }

    private SaveFiscalPreparationCommand validPreparationCommand() {
        return new SaveFiscalPreparationCommand(
                TENANT_ID,
                COMPANY_ID,
                "11.222.333/0001-81",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "110042490114",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "4712100",
                "01001000",
                "SP",
                35,
                "Sao Paulo",
                3550308,
                "Se",
                "Praca da Se",
                "100",
                null,
                "Brasil",
                1058,
                FiscalDocumentModel.NFCE,
                FiscalEnvironment.HOMOLOGACAO,
                "1",
                "CSC-HOMOLOGACAO",
                1,
                100L,
                "BASE64-PFX",
                "pfx-password",
                NOW.plusSeconds(3600)
        );
    }

    private FiscalIssuerProfile issuerProfile() {
        return new FiscalIssuerProfile(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                "11222333000181",
                "Kalles Comercio LTDA",
                "Kalles Matriz",
                "110042490114",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "4712100"
        );
    }

    private FiscalIssuerAddress issuerAddress() {
        return new FiscalIssuerAddress(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                "01001000",
                "SP",
                35,
                "Sao Paulo",
                3550308,
                "Se",
                "Praca da Se",
                "100",
                null,
                "Brasil",
                1058
        );
    }

    private FiscalCertificate validCertificate() {
        return new FiscalCertificate(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                NOW.plusSeconds(3600),
                true,
                "protected-content",
                "protected-password"
        );
    }
}
