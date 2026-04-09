package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentCommand;
import dev.kalles.sale.payment.domain.PaymentResult;

public interface ProcessPaymentUseCase {

    PaymentResult execute(PaymentCommand command);
}
