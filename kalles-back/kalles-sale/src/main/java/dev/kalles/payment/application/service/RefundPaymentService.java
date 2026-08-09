package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.in.RefundPaymentUseCase;
import dev.kalles.payment.domain.PaymentProvider;
import org.springframework.stereotype.Service;

@Service
public class RefundPaymentService implements RefundPaymentUseCase {

    private final PaymentProviderPortFactory portFactory;

    public RefundPaymentService(PaymentProviderPortFactory portFactory) {
        this.portFactory = portFactory;
    }

    @Override
    public void execute(PaymentProvider provider, String providerPaymentId) {
        portFactory.gateway(provider).refundPayment(providerPaymentId);
    }
}
