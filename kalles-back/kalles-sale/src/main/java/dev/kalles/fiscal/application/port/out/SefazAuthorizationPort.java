package dev.kalles.fiscal.application.port.out;

import dev.kalles.fiscal.domain.FiscalCertificate;
import dev.kalles.fiscal.domain.FiscalConfiguration;
import dev.kalles.fiscal.domain.FiscalSale;
import dev.kalles.fiscal.domain.SefazAuthorizationResult;

public interface SefazAuthorizationPort {
    SefazAuthorizationResult authorize(FiscalSale sale, FiscalConfiguration configuration, FiscalCertificate certificate);
}
