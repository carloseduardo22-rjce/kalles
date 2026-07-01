package dev.kalles.sale.fiscal.application.service;

import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.fiscal.application.port.in.IssueFiscalReturnCommand;
import dev.kalles.sale.fiscal.application.port.in.IssueFiscalReturnUseCase;
import dev.kalles.sale.fiscal.application.port.out.FiscalDocumentRepository;
import dev.kalles.sale.fiscal.application.port.out.FiscalRefundReader;
import dev.kalles.sale.fiscal.domain.FiscalDocument;
import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;
import dev.kalles.sale.fiscal.domain.SefazAuthorizationResult;
import dev.kalles.sale.fiscal.exception.FiscalConflictException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class IssueFiscalReturnService implements IssueFiscalReturnUseCase {

    private final FiscalDocumentRepository documentRepository;
    private final FiscalRefundReader refundReader;
    private final Clock clock;

    public IssueFiscalReturnService(FiscalDocumentRepository documentRepository, FiscalRefundReader refundReader, Clock clock) {
        this.documentRepository = documentRepository;
        this.refundReader = refundReader;
        this.clock = clock;
    }

    @Override
    public FiscalDocument issueReturn(IssueFiscalReturnCommand command) {
        FiscalDocument original = documentRepository
                .findAuthorizedBySale(command.tenantId(), command.companyId(), command.saleId(), FiscalDocumentModel.NFCE)
                .orElseThrow(() -> new FiscalConflictException("Documento fiscal original autorizado e obrigatorio para devolucao"));

        if (!refundReader.hasConfirmedRefund(command.tenantId(), command.companyId(), command.saleId())) {
            throw new FiscalConflictException("Reembolso confirmado e obrigatorio para nota de devolucao");
        }

        if (documentRepository.existsBySaleAndStatus(command.tenantId(), command.companyId(), command.saleId(),
                FiscalDocumentModel.NFE_DEVOLUCAO, FiscalDocumentStatus.AUTORIZADO)) {
            throw new FiscalConflictException("A venda ja possui nota fiscal de devolucao autorizada");
        }

        String xml = "<devolucao><referenciada>" + original.accessKey() + "</referenciada></devolucao>";
        FiscalDocument document = FiscalDocument.authorized(
                command.tenantId(),
                command.companyId(),
                command.saleId(),
                FiscalDocumentModel.NFE_DEVOLUCAO,
                FiscalEnvironment.HOMOLOGACAO,
                SefazAuthorizationResult.authorized("DEV-" + original.accessKey(), "DEV-HOM-" + command.saleId(), xml),
                Instant.now(clock)
        );
        return documentRepository.save(document);
    }
}
