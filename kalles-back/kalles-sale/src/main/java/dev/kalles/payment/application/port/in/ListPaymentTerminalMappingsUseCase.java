package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentTerminalMapping;

import java.util.List;

public interface ListPaymentTerminalMappingsUseCase {

    List<PaymentTerminalMapping> execute(PaymentProvider provider);
}
