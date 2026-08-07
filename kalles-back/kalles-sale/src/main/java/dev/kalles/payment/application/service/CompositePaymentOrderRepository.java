package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.application.port.out.ProviderPaymentOrderRepository;
import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.exception.PaymentProviderNotSupportedException;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CompositePaymentOrderRepository implements PaymentOrderRepository {

    private final Map<PaymentProvider, ProviderPaymentOrderRepository> repositories;

    public CompositePaymentOrderRepository(List<ProviderPaymentOrderRepository> repositories) {
        EnumMap<PaymentProvider, ProviderPaymentOrderRepository> indexed = new EnumMap<>(PaymentProvider.class);
        for (ProviderPaymentOrderRepository repository : repositories) {
            ProviderPaymentOrderRepository previous = indexed.put(repository.provider(), repository);
            if (previous != null) {
                throw new IllegalStateException("Multiple payment order repositories registered for provider " + repository.provider());
            }
        }
        this.repositories = Map.copyOf(indexed);
    }

    @Override
    public void save(PaymentOrder paymentOrder) {
        repositoryFor(paymentOrder.provider()).save(paymentOrder);
    }

    @Override
    public Optional<PaymentOrder> findByProviderOrderIdAndProvider(String providerOrderId, PaymentProvider provider) {
        return repositoryFor(provider).findByProviderOrderId(providerOrderId);
    }

    private ProviderPaymentOrderRepository repositoryFor(PaymentProvider provider) {
        ProviderPaymentOrderRepository repository = repositories.get(provider);
        if (repository == null) {
            throw new PaymentProviderNotSupportedException(provider, "PaymentOrderRepository");
        }
        return repository;
    }
}
