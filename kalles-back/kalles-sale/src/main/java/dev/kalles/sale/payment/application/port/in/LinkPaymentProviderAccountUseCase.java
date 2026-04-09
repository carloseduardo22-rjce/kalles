package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.application.port.in.command.LinkPaymentProviderAccountCommand;

public interface LinkPaymentProviderAccountUseCase {

    void execute(LinkPaymentProviderAccountCommand command);
}
