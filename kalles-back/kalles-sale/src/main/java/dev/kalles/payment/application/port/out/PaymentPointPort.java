package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentPointDescriptor;
import dev.kalles.payment.domain.PaymentPointView;
import dev.kalles.payment.domain.PaymentStore;

import java.util.List;

public interface PaymentPointPort extends ProviderAwarePort {

    PaymentPoint createPoint(PaymentPoint point, PaymentStore store, PaymentPointDescriptor descriptor);

    List<PaymentPointView> listPoints();
}
