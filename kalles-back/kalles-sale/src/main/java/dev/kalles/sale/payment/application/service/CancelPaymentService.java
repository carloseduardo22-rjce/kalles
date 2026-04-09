package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.payment.application.port.in.CancelPaymentUseCase;
import dev.kalles.sale.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStatus;
import org.springframework.stereotype.Service;

@Service
public class CancelPaymentService implements CancelPaymentUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentOrderRepository paymentOrderRepository;

    public CancelPaymentService(
            PaymentProviderPortFactory portFactory,
            PaymentOrderRepository paymentOrderRepository
    ) {
        this.portFactory = portFactory;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Override
    public void execute(PaymentProvider provider, String providerOrderId) {
        portFactory.gateway(provider).cancelPayment(providerOrderId);

        paymentOrderRepository.findByProviderOrderIdAndProvider(providerOrderId, provider)
                .ifPresent(existing -> paymentOrderRepository.save(existing.withStatus(PaymentStatus.CANCELED)));
    }
}
