package dev.kalles.sale.fiscal.support;

import dev.kalles.sale.fiscal.application.port.out.SefazAuthorizationPort;
import dev.kalles.sale.fiscal.domain.FiscalCertificate;
import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalSale;
import dev.kalles.sale.fiscal.domain.SefazAuthorizationResult;

public class ControllableSefazAuthorizationPort implements SefazAuthorizationPort {

    private String nextRejection;

    public void rejectNext(String reason) {
        this.nextRejection = reason;
    }

    public void reset() {
        this.nextRejection = null;
    }

    @Override
    public SefazAuthorizationResult authorize(FiscalSale sale, FiscalConfiguration configuration, FiscalCertificate certificate) {
        if (nextRejection != null) {
            String reason = nextRejection;
            nextRejection = null;
            return SefazAuthorizationResult.rejected(reason);
        }
        return SefazAuthorizationResult.authorized("NFCe-HOM-" + sale.id(), "HOM-135260000000001");
    }
}
