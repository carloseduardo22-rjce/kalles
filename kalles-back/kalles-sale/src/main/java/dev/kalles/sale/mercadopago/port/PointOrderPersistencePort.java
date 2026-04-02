package dev.kalles.sale.mercadopago.port;

import dev.kalles.sale.mercadopago.domain.PointOrder;
import java.util.Optional;

public interface PointOrderPersistencePort {
    void save(PointOrder pointOrder);
    Optional<PointOrder> findByOrderId(String orderId);
}
