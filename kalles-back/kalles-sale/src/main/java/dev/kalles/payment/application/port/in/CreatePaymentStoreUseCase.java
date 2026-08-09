package dev.kalles.payment.application.port.in;

import dev.kalles.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.payment.domain.PaymentStore;

public interface CreatePaymentStoreUseCase {

    PaymentStore execute(CreatePaymentStoreCommand command);
}
