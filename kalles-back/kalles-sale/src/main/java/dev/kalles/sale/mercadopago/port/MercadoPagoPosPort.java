package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
import java.util.List;
import java.util.Map;

public interface MercadoPagoPosPort {
    Long createPos(Caixa caixa, Company company);
    List<Map<String, Object>> fetchPos();
}
