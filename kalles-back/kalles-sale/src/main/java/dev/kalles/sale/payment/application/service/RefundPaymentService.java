package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.payment.application.port.in.RefundPaymentUseCase;
import dev.kalles.sale.payment.domain.PaymentProvider;
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
