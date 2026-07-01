package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalDocument;

public interface DanfeRendererPort {
    byte[] render(FiscalDocument document);
}
