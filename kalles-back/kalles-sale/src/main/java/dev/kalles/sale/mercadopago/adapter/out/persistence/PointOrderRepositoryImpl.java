package dev.kalles.sale.mercadopago.adapter.out.persistence;

import dev.kalles.sale.mercadopago.adapter.out.persistence.entity.MercadoPagoOrderEntity;
import dev.kalles.sale.mercadopago.adapter.out.persistence.repository.MercadoPagoOrderRepository;
import dev.kalles.sale.mercadopago.domain.PointOrder;
import dev.kalles.sale.mercadopago.port.PointOrderPersistencePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PointOrderRepositoryImpl implements PointOrderPersistencePort {

    private final MercadoPagoOrderRepository repository;

    public PointOrderRepositoryImpl(MercadoPagoOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(PointOrder pointOrder) {
        MercadoPagoOrderEntity entity = new MercadoPagoOrderEntity();
        entity.setOrderId(pointOrder.getOrderId());
        entity.setPaymentId(pointOrder.getPaymentId());
        entity.setStatus(pointOrder.getStatus());
        entity.setExternalReference(pointOrder.getExternalReference());
        entity.setAmount(pointOrder.getAmount());
        entity.setIdempotencyKey(pointOrder.getIdempotencyKey());
        
        repository.save(entity);
    }

    @Override
    public Optional<PointOrder> findByOrderId(String orderId) {
        return repository.findById(orderId).map(entity -> {
            PointOrder order = new PointOrder();
            order.setOrderId(entity.getOrderId());
            order.setPaymentId(entity.getPaymentId());
            order.setStatus(entity.getStatus());
            order.setExternalReference(entity.getExternalReference());
            order.setAmount(entity.getAmount());
            order.setIdempotencyKey(entity.getIdempotencyKey());
            return order;
        });
    }
}
