package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.application.port.in.command.CreatePaymentPointCommand;
import dev.kalles.sale.payment.domain.PaymentPoint;

public interface CreatePaymentPointUseCase {

    PaymentPoint execute(CreatePaymentPointCommand command);
}
