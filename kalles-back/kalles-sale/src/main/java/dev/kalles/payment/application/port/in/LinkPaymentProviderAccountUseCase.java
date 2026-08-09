package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.LinkPaymentProviderAccountCommand;

public interface LinkPaymentProviderAccountUseCase {

    void execute(LinkPaymentProviderAccountCommand command);
}
