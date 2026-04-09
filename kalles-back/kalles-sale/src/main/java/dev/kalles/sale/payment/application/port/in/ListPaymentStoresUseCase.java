package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStoreView;

import java.util.List;

public interface ListPaymentStoresUseCase {

    List<PaymentStoreView> execute(PaymentProvider provider);
}
