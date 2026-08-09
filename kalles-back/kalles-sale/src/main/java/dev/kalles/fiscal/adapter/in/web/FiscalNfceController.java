package dev.kalles.fiscal.adapter.in.web;

import dev.kalles.fiscal.adapter.in.web.dto.FiscalDocumentResponse;
import dev.kalles.fiscal.adapter.in.web.dto.IssueNfceRequest;
import dev.kalles.fiscal.application.port.in.IssueNfceCommand;
import dev.kalles.fiscal.application.port.in.IssueNfceUseCase;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fiscal/nfce")
@RequiredArgsConstructor
public class FiscalNfceController {

    private final IssueNfceUseCase issueNfceUseCase;

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public FiscalDocumentResponse issue(@Valid @RequestBody IssueNfceRequest request) {
        var document = issueNfceUseCase.issue(new IssueNfceCommand(
                TenantContextHolder.getTenantId(),
                CompanyContextHolder.getCompanyId(),
                request.saleId(),
                request.model(),
                request.environment()
        ));
        return FiscalDocumentResponse.from(document);
    }
}
