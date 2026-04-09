package dev.kalles.sale.payment.adapter.out.mercadopago.persistence;

import dev.kalles.sale.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoPaymentOrderEntity;
import dev.kalles.sale.payment.adapter.out.mercadopago.persistence.repository.MercadoPagoPaymentOrderJpaRepository;
import dev.kalles.sale.payment.application.port.out.ProviderPaymentOrderRepository;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentProvider;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MercadoPagoPaymentOrderRepositoryAdapter implements ProviderPaymentOrderRepository {

    private final MercadoPagoPaymentOrderJpaRepository repository;

    public MercadoPagoPaymentOrderRepositoryAdapter(MercadoPagoPaymentOrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public void save(PaymentOrder paymentOrder) {
        if (paymentOrder.provider() != PaymentProvider.MERCADO_PAGO) {
            throw new UnsupportedOperationException("This repository only supports Mercado Pago orders");
        }

        MercadoPagoPaymentOrderEntity entity = repository.findById(paymentOrder.providerOrderId())
                .orElse(new MercadoPagoPaymentOrderEntity());
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
                PaymentProvider.MERCADO_PAGO,
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
