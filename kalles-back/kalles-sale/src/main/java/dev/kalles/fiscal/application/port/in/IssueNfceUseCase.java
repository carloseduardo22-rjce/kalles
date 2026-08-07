package dev.kalles.fiscal.application.port.in;

import dev.kalles.fiscal.domain.FiscalDocument;

public interface IssueNfceUseCase {
    FiscalDocument issue(IssueNfceCommand command);
}
