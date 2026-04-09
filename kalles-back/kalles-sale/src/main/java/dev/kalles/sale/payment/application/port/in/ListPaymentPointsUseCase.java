package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentPointView;
import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.List;

public interface ListPaymentPointsUseCase {

    List<PaymentPointView> execute(PaymentProvider provider);
}
