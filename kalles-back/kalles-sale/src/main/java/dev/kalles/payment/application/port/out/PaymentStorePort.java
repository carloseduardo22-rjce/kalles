package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.MerchantProfile;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.payment.domain.PaymentStoreView;

import java.util.List;

public interface PaymentStorePort extends ProviderAwarePort {

    PaymentStore createStore(PaymentStore store, MerchantProfile merchantProfile);

    List<PaymentStoreView> listStores();
}
