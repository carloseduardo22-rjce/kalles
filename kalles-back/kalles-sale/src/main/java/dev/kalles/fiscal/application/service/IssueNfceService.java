package dev.kalles.fiscal.application.service;

import dev.kalles.fiscal.application.port.in.IssueNfceCommand;
import dev.kalles.fiscal.application.port.in.IssueNfceUseCase;
import dev.kalles.fiscal.application.port.out.FiscalCertificateRepository;
import dev.kalles.fiscal.application.port.out.FiscalConfigurationRepository;
import dev.kalles.fiscal.application.port.out.FiscalDocumentRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerAddressRepository;
import dev.kalles.fiscal.application.port.out.FiscalIssuerProfileRepository;
import dev.kalles.fiscal.application.port.out.FiscalSaleReader;
import dev.kalles.fiscal.application.port.out.SefazAuthorizationPort;
import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.exception.FiscalConflictException;
import dev.kalles.fiscal.exception.FiscalRejectionException;
import dev.kalles.fiscal.exception.FiscalValidationException;
import dev.kalles.shared.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class IssueNfceService implements IssueNfceUseCase {

    private final FiscalSaleReader saleReader;
    private final FiscalConfigurationRepository configurationRepository;
    private final FiscalCertificateRepository certificateRepository;
    private final FiscalDocumentRepository documentRepository;
    private final FiscalIssuerProfileRepository issuerProfileRepository;
    private final FiscalIssuerAddressRepository issuerAddressRepository;
    private final SefazAuthorizationPort sefazAuthorizationPort;
    private final Clock clock;

    public IssueNfceService(
            FiscalSaleReader saleReader,
            FiscalConfigurationRepository configurationRepository,
            FiscalCertificateRepository certificateRepository,
            FiscalDocumentRepository documentRepository,
            FiscalIssuerProfileRepository issuerProfileRepository,
            FiscalIssuerAddressRepository issuerAddressRepository,
            SefazAuthorizationPort sefazAuthorizationPort,
            Clock clock
    ) {
        this.saleReader = saleReader;
        this.configurationRepository = configurationRepository;
        this.certificateRepository = certificateRepository;
        this.documentRepository = documentRepository;
        this.issuerProfileRepository = issuerProfileRepository;
        this.issuerAddressRepository = issuerAddressRepository;
        this.sefazAuthorizationPort = sefazAuthorizationPort;
        this.clock = clock;
    }

    @Override
    public FiscalDocument issue(IssueNfceCommand command) {
        var sale = saleReader.findByIdForTenantAndCompany(command.saleId(), command.tenantId(), command.companyId())
                .orElseThrow(() -> new NotFoundException("Venda nao encontrada"));

        if (!sale.canIssueFiscalDocument()) {
            throw new FiscalConflictException("A NFC-e so pode ser emitida para venda finalizada e paga");
        }

        if (documentRepository.existsBySaleAndStatus(command.tenantId(), command.companyId(), command.saleId(),
                command.model(), FiscalDocumentStatus.AUTORIZADO)) {
            throw new FiscalConflictException("A venda ja possui documento fiscal autorizado");
        }

        var configuration = configurationRepository
                .findByCompany(command.tenantId(), command.companyId(), command.model(), command.environment())
                .orElseThrow(() -> new FiscalConflictException("Configuracao fiscal da filial nao encontrada"));

        Instant now = Instant.now(clock);
        var certificate = certificateRepository.findActiveByCompany(command.tenantId(), command.companyId())
                .filter(it -> it.isValidAt(now))
                .orElseThrow(() -> new FiscalConflictException("Certificado digital valido e obrigatorio para emissao fiscal"));

        if (command.environment() == FiscalEnvironment.PRODUCAO) {
            ensureProductionReadiness(command.tenantId(), command.companyId());
        }

        if (!sale.hasMinimumFiscalClassification()) {
            throw new FiscalValidationException("Todos os itens da NFC-e devem possuir classificacao fiscal minima");
        }

        long documentNumber = configurationRepository.reserveNextNumber(
                command.tenantId(), command.companyId(), command.model(), command.environment());

        var authorization = sefazAuthorizationPort.authorize(
                sale, configuration.withDocumentNumber(documentNumber), certificate);
        FiscalDocument document = authorization.authorized()
                ? FiscalDocument.authorized(command.tenantId(), command.companyId(), command.saleId(), command.model(),
                        command.environment(), authorization, now)
                : FiscalDocument.rejected(command.tenantId(), command.companyId(), command.saleId(), command.model(),
                        command.environment(), authorization, now);

        FiscalDocument savedDocument = saveEnforcingSingleAuthorization(document);
        if (!authorization.authorized()) {
            throw new FiscalRejectionException(savedDocument);
        }
        return savedDocument;
    }

    private FiscalDocument saveEnforcingSingleAuthorization(FiscalDocument document) {
        try {
            return documentRepository.save(document);
        } catch (DataIntegrityViolationException ex) {
            throw new FiscalConflictException("A venda ja possui documento fiscal autorizado");
        }
    }

    private void ensureProductionReadiness(UUID tenantId, UUID companyId) {
        boolean hasIssuerProfile = issuerProfileRepository.findByCompany(tenantId, companyId)
                .filter(profile -> profile.hasRequiredNfceIdentification())
                .isPresent();
        boolean hasIssuerAddress = issuerAddressRepository.findByCompany(tenantId, companyId)
                .filter(address -> address.isComplete())
                .isPresent();

        if (!hasIssuerProfile || !hasIssuerAddress) {
            throw new FiscalConflictException("Filial nao esta pronta para emissao fiscal");
        }
    }
}
