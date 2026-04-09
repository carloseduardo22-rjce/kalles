package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentPoint;
import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPointRepository {

    Optional<PaymentPoint> findById(UUID id);

    Optional<PaymentPoint> findByExternalReferenceAndProvider(String externalReference, PaymentProvider provider);

    Optional<PaymentPoint> findByCashRegisterIdAndProvider(UUID cashRegisterId, PaymentProvider provider);

    void save(PaymentPoint point);

    void updateProviderPointId(UUID id, String providerPointId);
}
