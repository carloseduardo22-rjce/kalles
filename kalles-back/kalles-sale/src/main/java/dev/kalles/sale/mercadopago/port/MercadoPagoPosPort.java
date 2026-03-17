package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;

public interface MercadoPagoPosPort {
    Long createPos(Caixa caixa, Company company);
}
