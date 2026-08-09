package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.payment.domain.PaymentTerminal;

import java.util.List;

public interface ListPaymentTerminalsUseCase {

    List<PaymentTerminal> execute(ListPaymentTerminalsQuery query);
}
