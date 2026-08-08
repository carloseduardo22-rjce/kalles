package dev.kalles.fiscal.application.service;

import dev.kalles.fiscal.application.port.in.FiscalDocumentQueryUseCase;
import dev.kalles.fiscal.application.port.out.DanfeRendererPort;
import dev.kalles.fiscal.application.port.out.FiscalDocumentRepository;
import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.exception.FiscalConflictException;
import dev.kalles.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FiscalDocumentQueryService implements FiscalDocumentQueryUseCase {

    private final FiscalDocumentRepository documentRepository;
    private final DanfeRendererPort danfeRendererPort;

    public FiscalDocumentQueryService(FiscalDocumentRepository documentRepository, DanfeRendererPort danfeRendererPort) {
        this.documentRepository = documentRepository;
        this.danfeRendererPort = danfeRendererPort;
    }

    @Override
    public FiscalDocument getStatus(UUID tenantId, UUID companyId, UUID documentId) {
        return documentRepository.findById(tenantId, companyId, documentId)
                .orElseThrow(() -> new NotFoundException("Documento fiscal nao encontrado"));
    }

    @Override
    public byte[] renderDanfe(UUID tenantId, UUID companyId, UUID documentId) {
        FiscalDocument document = getStatus(tenantId, companyId, documentId);
        if (document.status() != FiscalDocumentStatus.AUTORIZADO) {
            throw new FiscalConflictException("DANFE disponivel apenas para documento autorizado");
        }
        return danfeRendererPort.render(document);
    }
}
