package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStoreView;

import java.util.List;

public interface ListPaymentStoresUseCase {

    List<PaymentStoreView> execute(PaymentProvider provider);
}
