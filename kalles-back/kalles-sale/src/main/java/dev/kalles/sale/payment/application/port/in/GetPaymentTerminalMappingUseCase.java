package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.application.port.in.command.GetPaymentTerminalMappingQuery;
import dev.kalles.sale.payment.domain.PaymentTerminalMapping;

public interface GetPaymentTerminalMappingUseCase {

    PaymentTerminalMapping execute(GetPaymentTerminalMappingQuery query);
}
