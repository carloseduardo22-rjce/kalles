package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.CreatePaymentPointCommand;
import dev.kalles.payment.domain.PaymentPoint;

public interface CreatePaymentPointUseCase {

    PaymentPoint execute(CreatePaymentPointCommand command);
}
