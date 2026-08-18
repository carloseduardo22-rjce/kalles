package dev.kalles.payment.adapter.out.mercadopago.persistence;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoPointEntity;
import dev.kalles.payment.adapter.out.mercadopago.persistence.repository.MercadoPagoPointJpaRepository;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentProvider;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MercadoPagoPaymentPointRepositoryAdapter implements PaymentPointRepository {

    private final MercadoPagoPointJpaRepository repository;

    public MercadoPagoPaymentPointRepositoryAdapter(MercadoPagoPointJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentPoint> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PaymentPoint> findByExternalReferenceAndProvider(String externalReference, PaymentProvider provider) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            return Optional.empty();
        }
        return repository.findByExternalReference(externalReference).map(this::toDomain);
    }

    @Override
    public Optional<PaymentPoint> findByCashRegisterIdAndProvider(UUID cashRegisterId, PaymentProvider provider) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            return Optional.empty();
        }

        return repository.findFirstByCashRegisterId(cashRegisterId).map(this::toDomain);
    }

    @Override
    public void save(PaymentPoint point) {
        if (point.provider() != PaymentProvider.MERCADO_PAGO) {
            throw new UnsupportedOperationException("This repository only supports Mercado Pago points");
        }

        MercadoPagoPointEntity entity = resolveEntity(point);
        entity.setExternalReference(point.externalReference());
        entity.setCashRegisterId(point.cashRegisterId());
        entity.setProviderPointId(parseLong(point.providerPointId()));
        repository.save(entity);
    }

    @Override
    public void updateProviderPointId(UUID id, String providerPointId) {
        repository.findById(id).ifPresent(entity -> {
            entity.setProviderPointId(parseLong(providerPointId));
            repository.save(entity);
        });
    }

    private MercadoPagoPointEntity resolveEntity(PaymentPoint point) {
        if (point.id() != null) {
            return repository.findById(point.id()).orElse(new MercadoPagoPointEntity());
        }
        return repository.findByExternalReference(point.externalReference()).orElse(new MercadoPagoPointEntity());
    }

    private PaymentPoint toDomain(MercadoPagoPointEntity entity) {
        return new PaymentPoint(
                entity.getId(),
                entity.getCashRegisterId(),
                PaymentProvider.MERCADO_PAGO,
                entity.getExternalReference(),
                entity.getProviderPointId() != null ? String.valueOf(entity.getProviderPointId()) : null
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
