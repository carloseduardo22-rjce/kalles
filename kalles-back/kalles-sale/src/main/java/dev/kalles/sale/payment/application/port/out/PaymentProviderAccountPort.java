package dev.kalles.sale.payment.application.port.out;

import dev.kalles.sale.payment.domain.PaymentProviderAuthorization;

public interface PaymentProviderAccountPort extends ProviderAwarePort {

    PaymentProviderAuthorization exchangeAuthorizationCode(String authorizationCode);
}
