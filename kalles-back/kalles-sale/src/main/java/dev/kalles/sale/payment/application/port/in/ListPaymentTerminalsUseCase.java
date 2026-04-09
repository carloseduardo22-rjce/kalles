package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.sale.payment.domain.PaymentTerminal;

import java.util.List;

public interface ListPaymentTerminalsUseCase {

    List<PaymentTerminal> execute(ListPaymentTerminalsQuery query);
}
