package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.ActivatePaymentTerminalCommand;

public interface ActivatePaymentTerminalUseCase {

    void execute(ActivatePaymentTerminalCommand command);
}
