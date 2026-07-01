package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentTerminalMapping;

import java.util.List;

public interface ListPaymentTerminalMappingsUseCase {

    List<PaymentTerminalMapping> execute(PaymentProvider provider);
}
