package dev.kalles.sale.fiscal.application.port.out;

import dev.kalles.sale.fiscal.domain.FiscalCertificate;
import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalSale;
import dev.kalles.sale.fiscal.domain.SefazAuthorizationResult;

public interface SefazAuthorizationPort {
    SefazAuthorizationResult authorize(FiscalSale sale, FiscalConfiguration configuration, FiscalCertificate certificate);
}
