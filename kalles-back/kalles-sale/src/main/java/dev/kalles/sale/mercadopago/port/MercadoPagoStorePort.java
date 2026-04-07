package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Company;
import java.util.List;
import java.util.Map;

public interface MercadoPagoStorePort {
    Long createStore(Company company, dev.kalles.sale.core.entity.Company coreCompany);
    List<Map<String, Object>> fetchStores();
}
