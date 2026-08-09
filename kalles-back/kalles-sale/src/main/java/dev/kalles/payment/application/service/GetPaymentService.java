package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.in.GetPaymentUseCase;
import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import org.springframework.stereotype.Service;

@Service
public class GetPaymentService implements GetPaymentUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentOrderRepository paymentOrderRepository;

    public GetPaymentService(
            PaymentProviderPortFactory portFactory,
            PaymentOrderRepository paymentOrderRepository
    ) {
        this.portFactory = portFactory;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Override
    public PaymentResult execute(PaymentProvider provider, String providerOrderId) {
        PaymentResult result = portFactory.gateway(provider).getPayment(providerOrderId);

        paymentOrderRepository.findByProviderOrderIdAndProvider(providerOrderId, provider)
                .ifPresent(existing -> paymentOrderRepository.save(
                        existing.withStatus(result.status()).withProviderPaymentId(result.providerPaymentId())
                ));

        return result;
    }
}
