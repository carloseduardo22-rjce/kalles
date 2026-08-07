package dev.kalles.fiscal.application.service;

import dev.kalles.core.exception.NotFoundException;
import dev.kalles.fiscal.application.port.in.FiscalAdminUseCase;
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
import dev.kalles.fiscal.domain.FiscalIssuerAddress;
import dev.kalles.fiscal.domain.FiscalIssuerProfile;
import dev.kalles.fiscal.domain.FiscalProductClassification;
import dev.kalles.fiscal.domain.FiscalReadiness;
import dev.kalles.fiscal.exception.FiscalValidationException;
import dev.kalles.note.application.port.out.CryptoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FiscalAdminService implements FiscalAdminUseCase {

    private final FiscalCompanyAccessPort companyAccessPort;
    private final FiscalProductAccessPort productAccessPort;
    private final FiscalConfigurationRepository configurationRepository;
    private final FiscalCertificateRepository certificateRepository;
    private final FiscalProductClassificationRepository classificationRepository;
    private final FiscalIssuerProfileRepository issuerProfileRepository;
    private final FiscalIssuerAddressRepository issuerAddressRepository;
    private final CryptoPort cryptoPort;
    private final String cryptoSecret;
    private final Clock clock;

    public FiscalAdminService(
            FiscalCompanyAccessPort companyAccessPort,
            FiscalProductAccessPort productAccessPort,
            FiscalConfigurationRepository configurationRepository,
            FiscalCertificateRepository certificateRepository,
            FiscalProductClassificationRepository classificationRepository,
            FiscalIssuerProfileRepository issuerProfileRepository,
            FiscalIssuerAddressRepository issuerAddressRepository,
            CryptoPort cryptoPort,
            @Value("${security.encryption.tenant-credentials-secret}") String cryptoSecret,
            Clock clock
    ) {
        this.companyAccessPort = companyAccessPort;
        this.productAccessPort = productAccessPort;
        this.configurationRepository = configurationRepository;
        this.certificateRepository = certificateRepository;
        this.classificationRepository = classificationRepository;
        this.issuerProfileRepository = issuerProfileRepository;
        this.issuerAddressRepository = issuerAddressRepository;
        this.cryptoPort = cryptoPort;
        this.cryptoSecret = cryptoSecret;
        this.clock = clock;
    }

    @Override
    public FiscalIssuerProfile saveIssuerProfile(SaveFiscalIssuerProfileCommand command) {
        ensureCompany(command.tenantId(), command.companyId());
        if (!isValidCnpj(command.cnpj())) {
            throw new FiscalValidationException("CNPJ do emissor fiscal invalido");
        }
        if (!StringUtils.hasText(command.legalName())) {
            throw new FiscalValidationException("Razao social do emissor fiscal e obrigatoria");
        }
        if (command.taxRegime() == null) {
            throw new FiscalValidationException("Regime fiscal da filial e obrigatorio");
        }
        if (!StringUtils.hasText(command.stateRegistration())) {
            throw new FiscalValidationException("Inscricao estadual e obrigatoria para NFC-e");
        }

        return issuerProfileRepository.save(new FiscalIssuerProfile(
                null,
                command.tenantId(),
                command.companyId(),
                onlyDigits(command.cnpj()),
                command.legalName(),
                command.tradeName(),
                onlyDigits(command.stateRegistration()),
                command.taxRegime(),
                onlyDigits(command.cnae())
        ));
    }

    @Override
    public FiscalIssuerAddress saveIssuerAddress(SaveFiscalIssuerAddressCommand command) {
        ensureCompany(command.tenantId(), command.companyId());
        if (!StringUtils.hasText(command.stateCode())) {
            throw new FiscalValidationException("UF do endereco fiscal e obrigatoria");
        }
        if (command.cityIbgeCode() == null || command.stateIbgeCode() == null) {
            throw new FiscalValidationException("Codigo IBGE do endereco fiscal e obrigatorio");
        }

        FiscalIssuerAddress address = new FiscalIssuerAddress(
                null,
                command.tenantId(),
                command.companyId(),
                onlyDigits(command.zipCode()),
                command.stateCode(),
                command.stateIbgeCode(),
                command.cityName(),
                command.cityIbgeCode(),
                command.district(),
                command.street(),
                command.number(),
                command.complement(),
                StringUtils.hasText(command.countryName()) ? command.countryName() : "Brasil",
                command.countryCode() != null ? command.countryCode() : 1058
        );

        if (!address.isComplete()) {
            throw new FiscalValidationException("Endereco fiscal completo e obrigatorio");
        }

        return issuerAddressRepository.save(address);
    }

    @Override
    @Transactional
    public FiscalReadiness savePreparation(SaveFiscalPreparationCommand command) {
        ensureCompany(command.tenantId(), command.companyId());
        FiscalIssuerProfile profile = buildIssuerProfile(command);
        FiscalIssuerAddress address = buildIssuerAddress(command);
        FiscalConfiguration configuration = buildConfiguration(command);
        FiscalCertificate certificate = buildCertificate(command);

        issuerProfileRepository.save(profile);
        issuerAddressRepository.save(address);
        configurationRepository.save(configuration);
        certificateRepository.deactivateActiveByCompany(command.tenantId(), command.companyId());
        certificateRepository.save(certificate);

        return getReadiness(command.tenantId(), command.companyId());
    }

    @Override
    public FiscalConfiguration saveConfiguration(SaveFiscalConfigurationCommand command) {
        ensureCompany(command.tenantId(), command.companyId());
        if (!StringUtils.hasText(command.stateCode())) {
            throw new FiscalValidationException("UF da configuracao fiscal e obrigatoria");
        }
        if (command.series() == null || command.series() <= 0) {
            throw new FiscalValidationException("Serie fiscal deve ser positiva");
        }
        if (command.nextNumber() == null || command.nextNumber() <= 0) {
            throw new FiscalValidationException("Proximo numero fiscal deve ser positivo");
        }

        return configurationRepository.save(new FiscalConfiguration(
                null,
                command.tenantId(),
                command.companyId(),
                command.model(),
                command.environment(),
                command.stateCode(),
                command.cscId(),
                command.cscToken(),
                command.series(),
                command.nextNumber()
        ));
    }

    @Override
    public FiscalCertificate registerCertificate(RegisterFiscalCertificateCommand command) {
        ensureCompany(command.tenantId(), command.companyId());
        if (command.expiresAt() == null || !command.expiresAt().isAfter(Instant.now(clock))) {
            throw new FiscalValidationException("Certificado digital expirado");
        }
        if (!StringUtils.hasText(command.certificateBase64()) || !StringUtils.hasText(command.password())) {
            throw new FiscalValidationException("Certificado digital e senha sao obrigatorios");
        }

        certificateRepository.deactivateActiveByCompany(command.tenantId(), command.companyId());
        return certificateRepository.save(new FiscalCertificate(
                null,
                command.tenantId(),
                command.companyId(),
                command.expiresAt(),
                true,
                cryptoPort.encrypt(command.certificateBase64(), cryptoSecret),
                cryptoPort.encrypt(command.password(), cryptoSecret)
        ));
    }

    @Override
    public FiscalProductClassification saveProductClassification(SaveFiscalProductClassificationCommand command) {
        ensureCompany(command.tenantId(), command.companyId());
        if (!productAccessPort.existsProductByTenant(command.tenantId(), command.productId())) {
            throw new NotFoundException("Produto nao encontrado");
        }
        if (!StringUtils.hasText(command.ncm())) {
            throw new FiscalValidationException("NCM e obrigatorio");
        }

        FiscalProductClassification classification = new FiscalProductClassification(
                null,
                command.tenantId(),
                command.companyId(),
                command.productId(),
                command.ncm(),
                command.cest(),
                command.cfop(),
                StringUtils.hasText(command.cfopSale()) ? command.cfopSale() : command.cfop(),
                command.origin(),
                command.csosn(),
                command.cst(),
                command.unit(),
                command.gtin()
        );

        Optional.ofNullable(issuerProfileRepository.findByCompany(command.tenantId(), command.companyId()))
                .flatMap(profile -> profile)
                .ifPresent(profile -> {
                    if (!classification.isCompatibleWith(profile.taxRegime())) {
                        throw new FiscalValidationException("Tributacao do produto incompativel com o regime fiscal da filial");
                    }
                });

        return classificationRepository.save(classification);
    }

    @Override
    public FiscalReadiness getReadiness(UUID tenantId, UUID companyId) {
        ensureCompany(tenantId, companyId);
        List<String> missingItems = new ArrayList<>();
        FiscalIssuerProfile profile = Optional.ofNullable(issuerProfileRepository.findByCompany(tenantId, companyId))
                .flatMap(found -> found)
                .orElse(null);
        FiscalIssuerAddress address = Optional.ofNullable(issuerAddressRepository.findByCompany(tenantId, companyId))
                .flatMap(found -> found)
                .orElse(null);

        if (profile == null || !profile.hasRequiredNfceIdentification()) {
            missingItems.add("Dados fiscais da filial");
        }
        if (address == null || !address.isComplete()) {
            missingItems.add("Endereco fiscal da filial");
        }
        if (certificateRepository.findActiveByCompany(tenantId, companyId)
                .filter(certificate -> certificate.isValidAt(Instant.now(clock)))
                .isEmpty()) {
            missingItems.add("Certificado A1 valido");
        }

        return new FiscalReadiness(tenantId, companyId, missingItems.isEmpty(), missingItems);
    }

    private void ensureCompany(UUID tenantId, UUID companyId) {
        if (!companyAccessPort.existsByTenant(tenantId, companyId)) {
            throw new NotFoundException("Filial nao encontrada");
        }
    }

    private FiscalIssuerProfile buildIssuerProfile(SaveFiscalPreparationCommand command) {
        if (!isValidCnpj(command.cnpj())) {
            throw new FiscalValidationException("CNPJ do emissor fiscal invalido");
        }
        if (!StringUtils.hasText(command.legalName())) {
            throw new FiscalValidationException("Razao social do emissor fiscal e obrigatoria");
        }
        if (command.taxRegime() == null) {
            throw new FiscalValidationException("Regime fiscal da filial e obrigatorio");
        }
        if (!StringUtils.hasText(command.stateRegistration())) {
            throw new FiscalValidationException("Inscricao estadual e obrigatoria para NFC-e");
        }

        return new FiscalIssuerProfile(
                null,
                command.tenantId(),
                command.companyId(),
                onlyDigits(command.cnpj()),
                command.legalName(),
                command.tradeName(),
                onlyDigits(command.stateRegistration()),
                command.taxRegime(),
                onlyDigits(command.cnae())
        );
    }

    private FiscalIssuerAddress buildIssuerAddress(SaveFiscalPreparationCommand command) {
        if (!StringUtils.hasText(command.stateCode())) {
            throw new FiscalValidationException("UF do endereco fiscal e obrigatoria");
        }
        if (command.cityIbgeCode() == null || command.stateIbgeCode() == null) {
            throw new FiscalValidationException("Codigo IBGE do endereco fiscal e obrigatorio");
        }

        FiscalIssuerAddress address = new FiscalIssuerAddress(
                null,
                command.tenantId(),
                command.companyId(),
                onlyDigits(command.zipCode()),
                command.stateCode(),
                command.stateIbgeCode(),
                command.cityName(),
                command.cityIbgeCode(),
                command.district(),
                command.street(),
                command.number(),
                command.complement(),
                StringUtils.hasText(command.countryName()) ? command.countryName() : "Brasil",
                command.countryCode() != null ? command.countryCode() : 1058
        );

        if (!address.isComplete()) {
            throw new FiscalValidationException("Endereco fiscal completo e obrigatorio");
        }
        return address;
    }

    private FiscalConfiguration buildConfiguration(SaveFiscalPreparationCommand command) {
        if (command.model() == null || command.environment() == null) {
            throw new FiscalValidationException("Modelo e ambiente fiscal sao obrigatorios");
        }
        if (!StringUtils.hasText(command.stateCode())) {
            throw new FiscalValidationException("UF da configuracao fiscal e obrigatoria");
        }
        if (command.series() == null || command.series() <= 0) {
            throw new FiscalValidationException("Serie fiscal deve ser positiva");
        }
        if (command.nextNumber() == null || command.nextNumber() <= 0) {
            throw new FiscalValidationException("Proximo numero fiscal deve ser positivo");
        }

        return new FiscalConfiguration(
                null,
                command.tenantId(),
                command.companyId(),
                command.model(),
                command.environment(),
                command.stateCode(),
                command.cscId(),
                command.cscToken(),
                command.series(),
                command.nextNumber()
        );
    }

    private FiscalCertificate buildCertificate(SaveFiscalPreparationCommand command) {
        if (command.certificateExpiresAt() == null || !command.certificateExpiresAt().isAfter(Instant.now(clock))) {
            throw new FiscalValidationException("Certificado digital expirado");
        }
        if (!StringUtils.hasText(command.certificateBase64()) || !StringUtils.hasText(command.certificatePassword())) {
            throw new FiscalValidationException("Certificado digital e senha sao obrigatorios");
        }

        return new FiscalCertificate(
                null,
                command.tenantId(),
                command.companyId(),
                command.certificateExpiresAt(),
                true,
                cryptoPort.encrypt(command.certificateBase64(), cryptoSecret),
                cryptoPort.encrypt(command.certificatePassword(), cryptoSecret)
        );
    }

    private boolean isValidCnpj(String value) {
        String cnpj = onlyDigits(value);
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        return calculateCnpjDigit(cnpj, 12) == Character.digit(cnpj.charAt(12), 10)
                && calculateCnpjDigit(cnpj, 13) == Character.digit(cnpj.charAt(13), 10);
    }

    private int calculateCnpjDigit(String cnpj, int length) {
        int[] weights = length == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.digit(cnpj.charAt(i), 10) * weights[i];
        }
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }

    private String onlyDigits(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

}
