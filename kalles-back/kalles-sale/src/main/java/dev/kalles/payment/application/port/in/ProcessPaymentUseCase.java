package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentResult;

public interface ProcessPaymentUseCase {

    PaymentResult execute(PaymentCommand command);
}
