package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentProviderAuthorization;

public interface PaymentProviderAccountPort extends ProviderAwarePort {

    PaymentProviderAuthorization exchangeAuthorizationCode(String authorizationCode);
}
