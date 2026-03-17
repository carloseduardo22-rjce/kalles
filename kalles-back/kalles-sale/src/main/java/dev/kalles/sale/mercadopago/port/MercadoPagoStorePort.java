package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.Company;

public interface MercadoPagoStorePort {
    Long createStore(Company company);
}
