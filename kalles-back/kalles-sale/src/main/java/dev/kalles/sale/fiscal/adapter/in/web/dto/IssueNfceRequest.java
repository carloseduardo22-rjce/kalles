package dev.kalles.sale.fiscal.adapter.in.web.dto;

import dev.kalles.sale.fiscal.domain.FiscalDocumentModel;
import dev.kalles.sale.fiscal.domain.FiscalEnvironment;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueNfceRequest(
        @NotNull UUID saleId,
        @NotNull FiscalDocumentModel model,
        @NotNull FiscalEnvironment environment
) {
}
