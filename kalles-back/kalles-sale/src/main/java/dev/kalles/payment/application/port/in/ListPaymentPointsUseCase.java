package dev.kalles.payment.application.port.in;

import dev.kalles.payment.domain.PaymentPointView;
import dev.kalles.payment.domain.PaymentProvider;

import java.util.List;

public interface ListPaymentPointsUseCase {

    List<PaymentPointView> execute(PaymentProvider provider);
}
