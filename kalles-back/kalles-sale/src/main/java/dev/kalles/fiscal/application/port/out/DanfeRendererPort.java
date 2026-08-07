package dev.kalles.fiscal.application.port.out;

import dev.kalles.fiscal.domain.FiscalDocument;

public interface DanfeRendererPort {
    byte[] render(FiscalDocument document);
}
