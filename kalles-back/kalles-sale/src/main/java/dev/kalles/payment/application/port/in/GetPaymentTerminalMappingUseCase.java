package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.GetPaymentTerminalMappingQuery;
import dev.kalles.payment.domain.PaymentTerminalMapping;

public interface GetPaymentTerminalMappingUseCase {

    PaymentTerminalMapping execute(GetPaymentTerminalMappingQuery query);
}
