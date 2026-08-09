package dev.kalles.payment.adapter.out.mercadopago.persistence;

import dev.kalles.payment.adapter.out.mercadopago.persistence.entity.MercadoPagoStoreEntity;
import dev.kalles.payment.adapter.out.mercadopago.persistence.repository.MercadoPagoStoreJpaRepository;
import dev.kalles.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MercadoPagoPaymentStoreRepositoryAdapter implements PaymentStoreRepository {

    private final MercadoPagoStoreJpaRepository repository;

    public MercadoPagoPaymentStoreRepositoryAdapter(MercadoPagoStoreJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentStore> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PaymentStore> findByExternalReferenceAndProvider(String externalReference, PaymentProvider provider) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            return Optional.empty();
        }
        return repository.findByExternalReference(externalReference).map(this::toDomain);
    }

    @Override
    public Optional<PaymentStore> findByCompanyIdAndProvider(UUID companyId, PaymentProvider provider) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            return Optional.empty();
        }
        return repository.findByCompanyId(companyId).map(this::toDomain);
    }

    @Override
    public void save(PaymentStore store) {
        if (store.provider() != PaymentProvider.MERCADO_PAGO) {
            throw new UnsupportedOperationException("This repository only supports Mercado Pago stores");
        }

        MercadoPagoStoreEntity entity = resolveEntity(store);
        entity.setCompanyId(store.companyId());
        entity.setExternalReference(store.externalReference());
        entity.setProviderStoreId(parseLong(store.providerStoreId()));
        repository.save(entity);
    }

    @Override
    public void updateProviderStoreId(UUID id, String providerStoreId) {
        repository.findById(id).ifPresent(entity -> {
            entity.setProviderStoreId(parseLong(providerStoreId));
            repository.save(entity);
        });
    }

    private MercadoPagoStoreEntity resolveEntity(PaymentStore store) {
        if (store.id() != null) {
            return repository.findById(store.id()).orElse(new MercadoPagoStoreEntity());
        }
        return repository.findByExternalReference(store.externalReference()).orElse(new MercadoPagoStoreEntity());
    }

    private PaymentStore toDomain(MercadoPagoStoreEntity entity) {
        return new PaymentStore(
                entity.getId(),
                entity.getCompanyId(),
                PaymentProvider.MERCADO_PAGO,
                entity.getExternalReference(),
                entity.getProviderStoreId() != null ? String.valueOf(entity.getProviderStoreId()) : null
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
