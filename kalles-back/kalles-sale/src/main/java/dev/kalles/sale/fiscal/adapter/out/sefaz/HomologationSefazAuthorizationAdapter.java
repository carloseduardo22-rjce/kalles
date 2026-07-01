package dev.kalles.sale.fiscal.adapter.out.sefaz;

import dev.kalles.sale.fiscal.application.port.out.SefazAuthorizationPort;
import dev.kalles.sale.fiscal.domain.FiscalCertificate;
import dev.kalles.sale.fiscal.domain.FiscalConfiguration;
import dev.kalles.sale.fiscal.domain.FiscalSale;
import dev.kalles.sale.fiscal.domain.SefazAuthorizationResult;

import java.util.UUID;

public class HomologationSefazAuthorizationAdapter implements SefazAuthorizationPort {

    @Override
    public SefazAuthorizationResult authorize(FiscalSale sale, FiscalConfiguration configuration, FiscalCertificate certificate) {
        String accessKey = "NFCe-HOM-" + compact(sale.id());
        String protocol = "HOM-" + compact(UUID.randomUUID());
        return SefazAuthorizationResult.authorized(accessKey, protocol, "<NFe><infNFe Id=\"" + accessKey + "\"/></NFe>");
    }

    private String compact(UUID id) {
        return id.toString().replace("-", "");
    }
}
