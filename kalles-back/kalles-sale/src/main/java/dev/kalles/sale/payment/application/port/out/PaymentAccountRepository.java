package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentProviderAccount;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAccountRepository {

    Optional<PaymentProviderAccount> findByTenantIdAndProvider(UUID tenantId, PaymentProvider provider);

    void save(PaymentProviderAccount account);
}
