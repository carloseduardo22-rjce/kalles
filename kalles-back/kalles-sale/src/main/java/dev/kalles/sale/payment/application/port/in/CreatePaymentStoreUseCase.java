package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.sale.payment.domain.PaymentStore;

public interface CreatePaymentStoreUseCase {

    PaymentStore execute(CreatePaymentStoreCommand command);
}
