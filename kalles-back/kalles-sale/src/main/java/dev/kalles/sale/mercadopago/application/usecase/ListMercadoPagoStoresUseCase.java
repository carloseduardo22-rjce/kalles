package dev.kalles.sale.mercadopago.application.usecase;

import dev.kalles.sale.mercadopago.port.MercadoPagoPosPort;
import dev.kalles.sale.mercadopago.port.MercadoPagoStorePort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListMercadoPagoStoresUseCase {

    private final MercadoPagoStorePort mercadoPagoStorePort;
    private final MercadoPagoPosPort mercadoPagoPosPort;

    public ListMercadoPagoStoresUseCase(MercadoPagoStorePort mercadoPagoStorePort, MercadoPagoPosPort mercadoPagoPosPort) {
        this.mercadoPagoStorePort = mercadoPagoStorePort;
        this.mercadoPagoPosPort = mercadoPagoPosPort;
    }

    public List<Map<String, Object>> execute() {
        List<Map<String, Object>> stores = mercadoPagoStorePort.fetchStores();
        List<Map<String, Object>> allPos = mercadoPagoPosPort.fetchPos();

        for (Map<String, Object> store : stores) {
            Object storeId = store.get("id");
            List<Map<String, Object>> storeTerminals = new ArrayList<>();

            for (Map<String, Object> pos : allPos) {
                if (String.valueOf(pos.get("store_id")).equals(String.valueOf(storeId))) {
                    storeTerminals.add(pos);
                }
            }
            store.put("terminals", storeTerminals);
        }

        return stores;
    }
}
