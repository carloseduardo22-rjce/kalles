package dev.kalles.fiscal.application.service;

import dev.kalles.fiscal.application.port.in.IssueFiscalReturnCommand;
import dev.kalles.fiscal.application.port.out.DanfeRendererPort;
import dev.kalles.fiscal.application.port.out.FiscalDocumentRepository;
import dev.kalles.fiscal.application.port.out.FiscalRefundReader;
import dev.kalles.fiscal.domain.FiscalDocument;
import dev.kalles.fiscal.domain.FiscalDocumentModel;
import dev.kalles.fiscal.domain.FiscalDocumentStatus;
import dev.kalles.fiscal.domain.FiscalEnvironment;
import dev.kalles.fiscal.exception.FiscalConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class FiscalDocumentOperationServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");
    private static final UUID COMPANY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174112");
    private static final UUID SALE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174113");
    private static final UUID DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174114");
    private static final Instant NOW = Instant.parse("2026-04-30T12:00:00Z");

    @Mock
    private FiscalDocumentRepository documentRepository;

    @Mock
    private FiscalRefundReader refundReader;

    @Mock
    private DanfeRendererPort danfeRendererPort;

    private IssueFiscalReturnService returnService;
    private FiscalDocumentQueryService queryService;

    @BeforeEach
    void setUp() {
        returnService = new IssueFiscalReturnService(documentRepository, refundReader, Clock.fixed(NOW, ZoneOffset.UTC));
        queryService = new FiscalDocumentQueryService(documentRepository, danfeRendererPort);
    }

    @Test
    void shouldIssueReturnWhenOriginalNfceAndRefundExist() {
        FiscalDocument original = authorizedDocument(FiscalDocumentModel.NFCE);
        when(documentRepository.findAuthorizedBySale(TENANT_ID, COMPANY_ID, SALE_ID, FiscalDocumentModel.NFCE))
                .thenReturn(Optional.of(original));
        when(refundReader.hasConfirmedRefund(TENANT_ID, COMPANY_ID, SALE_ID)).thenReturn(true);
        when(documentRepository.existsBySaleAndStatus(TENANT_ID, COMPANY_ID, SALE_ID,
                FiscalDocumentModel.NFE_DEVOLUCAO, FiscalDocumentStatus.AUTORIZADO)).thenReturn(false);
        when(documentRepository.save(any(FiscalDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FiscalDocument document = returnService.issueReturn(new IssueFiscalReturnCommand(TENANT_ID, COMPANY_ID, SALE_ID));

        assertThat(document.model()).isEqualTo(FiscalDocumentModel.NFE_DEVOLUCAO);
        assertThat(document.status()).isEqualTo(FiscalDocumentStatus.AUTORIZADO);
        assertThat(document.authorizedXml()).contains(original.accessKey());
    }

    @Test
    void shouldRejectReturnWithoutConfirmedRefund() {
        when(documentRepository.findAuthorizedBySale(TENANT_ID, COMPANY_ID, SALE_ID, FiscalDocumentModel.NFCE))
                .thenReturn(Optional.of(authorizedDocument(FiscalDocumentModel.NFCE)));
        when(refundReader.hasConfirmedRefund(TENANT_ID, COMPANY_ID, SALE_ID)).thenReturn(false);

        assertThatThrownBy(() -> returnService.issueReturn(new IssueFiscalReturnCommand(TENANT_ID, COMPANY_ID, SALE_ID)))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("Reembolso confirmado e obrigatorio para nota de devolucao");

        verify(documentRepository, never()).save(any());
    }

    @Test
    void shouldRenderDanfeOnlyForAuthorizedDocument() {
        FiscalDocument document = authorizedDocument(FiscalDocumentModel.NFCE);
        byte[] pdf = "%PDF-1.4".getBytes();
        when(documentRepository.findById(TENANT_ID, COMPANY_ID, DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(danfeRendererPort.render(document)).thenReturn(pdf);

        assertThat(queryService.renderDanfe(TENANT_ID, COMPANY_ID, DOCUMENT_ID)).isEqualTo(pdf);
    }

    @Test
    void shouldRejectDanfeForRejectedDocument() {
        FiscalDocument rejected = new FiscalDocument(DOCUMENT_ID, TENANT_ID, COMPANY_ID, SALE_ID,
                FiscalDocumentModel.NFCE, FiscalEnvironment.HOMOLOGACAO, FiscalDocumentStatus.REJEITADO,
                null, null, "Rejeicao", null, NOW);
        when(documentRepository.findById(TENANT_ID, COMPANY_ID, DOCUMENT_ID)).thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> queryService.renderDanfe(TENANT_ID, COMPANY_ID, DOCUMENT_ID))
                .isInstanceOf(FiscalConflictException.class)
                .hasMessage("DANFE disponivel apenas para documento autorizado");
    }

    private FiscalDocument authorizedDocument(FiscalDocumentModel model) {
        return new FiscalDocument(DOCUMENT_ID, TENANT_ID, COMPANY_ID, SALE_ID,
                model, FiscalEnvironment.HOMOLOGACAO, FiscalDocumentStatus.AUTORIZADO,
                "NFCe-HOM-" + SALE_ID, "HOM-135260000000001", null,
                "<NFe><infNFe Id=\"NFCe-HOM-" + SALE_ID + "\"/></NFe>", NOW);
    }
}
