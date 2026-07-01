package dev.kalles.sale.fiscal.application.port.in;

import dev.kalles.sale.fiscal.domain.FiscalDocument;

public interface IssueNfceUseCase {
    FiscalDocument issue(IssueNfceCommand command);
}
