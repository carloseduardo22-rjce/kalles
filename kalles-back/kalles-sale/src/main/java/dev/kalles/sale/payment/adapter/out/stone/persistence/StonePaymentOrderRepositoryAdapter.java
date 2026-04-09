package dev.kalles.sale.payment.adapter.out.stone.persistence;

import dev.kalles.sale.payment.adapter.out.stone.persistence.entity.StonePaymentOrderEntity;
import dev.kalles.sale.payment.adapter.out.stone.persistence.repository.StonePaymentOrderJpaRepository;
import dev.kalles.sale.payment.application.port.out.ProviderPaymentOrderRepository;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentProvider;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StonePaymentOrderRepositoryAdapter implements ProviderPaymentOrderRepository {

    private final StonePaymentOrderJpaRepository repository;

    public StonePaymentOrderRepositoryAdapter(StonePaymentOrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.STONE;
    }

    @Override
    public void save(PaymentOrder paymentOrder) {
        if (paymentOrder.provider() != PaymentProvider.STONE) {
            throw new UnsupportedOperationException("This repository only supports Stone orders");
        }

        StonePaymentOrderEntity entity = repository.findById(paymentOrder.providerOrderId())
                .orElse(new StonePaymentOrderEntity());
        entity.setProviderOrderId(paymentOrder.providerOrderId());
        entity.setProviderPaymentId(paymentOrder.providerPaymentId());
        entity.setStatus(paymentOrder.status());
        entity.setExternalReference(paymentOrder.externalReference());
        entity.setAmount(paymentOrder.amount());
        entity.setIdempotencyKey(paymentOrder.idempotencyKey());
        repository.save(entity);
    }

    @Override
    public Optional<PaymentOrder> findByProviderOrderId(String providerOrderId) {
        return repository.findById(providerOrderId).map(entity -> new PaymentOrder(
                PaymentProvider.STONE,
                entity.getProviderOrderId(),
                entity.getProviderPaymentId(),
                entity.getStatus(),
                entity.getExternalReference(),
                entity.getAmount(),
                entity.getIdempotencyKey(),
                PaymentFlow.TERMINAL,
                PaymentMethodType.UNSPECIFIED
        ));
    }
}
