package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentPoint;
import dev.kalles.sale.payment.domain.PaymentPointDescriptor;
import dev.kalles.sale.payment.domain.PaymentPointView;
import dev.kalles.sale.payment.domain.PaymentStore;

import java.util.List;

public interface PaymentPointPort extends ProviderAwarePort {

    PaymentPoint createPoint(PaymentPoint point, PaymentStore store, PaymentPointDescriptor descriptor);

    List<PaymentPointView> listPoints();
}
