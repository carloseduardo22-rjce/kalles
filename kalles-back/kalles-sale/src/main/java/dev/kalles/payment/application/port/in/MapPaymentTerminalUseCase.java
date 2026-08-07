package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.MapPaymentTerminalCommand;
import dev.kalles.payment.domain.PaymentTerminalMapping;

public interface MapPaymentTerminalUseCase {

    PaymentTerminalMapping execute(MapPaymentTerminalCommand command);
}
