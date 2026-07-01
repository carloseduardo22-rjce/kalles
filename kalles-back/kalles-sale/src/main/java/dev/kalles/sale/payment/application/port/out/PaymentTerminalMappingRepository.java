package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentTerminalMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTerminalMappingRepository {

    Optional<PaymentTerminalMapping> findActiveByCashRegisterIdAndProvider(UUID cashRegisterId, PaymentProvider provider);

    Optional<PaymentTerminalMapping> findActiveByCompanyIdAndProviderAndTerminalSerial(
            UUID companyId,
            PaymentProvider provider,
            String terminalSerial
    );

    List<PaymentTerminalMapping> findActiveByCompanyIdAndProvider(UUID companyId, PaymentProvider provider);

    PaymentTerminalMapping save(PaymentTerminalMapping mapping);
}
