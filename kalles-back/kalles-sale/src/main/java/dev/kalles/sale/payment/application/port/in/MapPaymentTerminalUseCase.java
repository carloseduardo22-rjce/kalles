package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.application.port.in.command.MapPaymentTerminalCommand;
import dev.kalles.sale.payment.domain.PaymentTerminalMapping;

public interface MapPaymentTerminalUseCase {

    PaymentTerminalMapping execute(MapPaymentTerminalCommand command);
}
