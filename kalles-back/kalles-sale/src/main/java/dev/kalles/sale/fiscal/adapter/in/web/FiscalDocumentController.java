package dev.kalles.sale.fiscal.adapter.in.web;

import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalDocumentResponse;
import dev.kalles.sale.fiscal.adapter.in.web.dto.IssueFiscalReturnRequest;
import dev.kalles.sale.fiscal.application.port.in.FiscalDocumentQueryUseCase;
import dev.kalles.sale.fiscal.application.port.in.IssueFiscalReturnCommand;
import dev.kalles.sale.fiscal.application.port.in.IssueFiscalReturnUseCase;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/fiscal")
@RequiredArgsConstructor
public class FiscalDocumentController {

    private final FiscalDocumentQueryUseCase documentQueryUseCase;
    private final IssueFiscalReturnUseCase issueFiscalReturnUseCase;

    @GetMapping("/documents/{documentId}/status")
    public FiscalDocumentResponse status(@PathVariable UUID documentId) {
        return FiscalDocumentResponse.from(documentQueryUseCase.getStatus(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                documentId
        ));
    }

    @GetMapping("/documents/{documentId}/danfe")
    public ResponseEntity<byte[]> danfe(@PathVariable UUID documentId) {
        byte[] pdf = documentQueryUseCase.renderDanfe(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                documentId
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=danfe-nfce.pdf")
                .body(pdf);
    }

    @PostMapping("/returns/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalDocumentResponse issueReturn(@Valid @RequestBody IssueFiscalReturnRequest request) {
        return FiscalDocumentResponse.from(issueFiscalReturnUseCase.issueReturn(new IssueFiscalReturnCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.saleId()
        )));
    }
}
