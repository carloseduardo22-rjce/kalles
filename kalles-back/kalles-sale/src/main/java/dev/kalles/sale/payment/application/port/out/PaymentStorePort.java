package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.MerchantProfile;
import dev.kalles.sale.payment.domain.PaymentStore;
import dev.kalles.sale.payment.domain.PaymentStoreView;

import java.util.List;

public interface PaymentStorePort extends ProviderAwarePort {

    PaymentStore createStore(PaymentStore store, MerchantProfile merchantProfile);

    List<PaymentStoreView> listStores();
}
