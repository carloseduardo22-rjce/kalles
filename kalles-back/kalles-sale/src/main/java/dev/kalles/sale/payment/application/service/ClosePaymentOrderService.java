package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.payment.application.port.in.ClosePaymentOrderUseCase;
import dev.kalles.sale.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentResult;
import dev.kalles.sale.payment.domain.PaymentStatus;
import org.springframework.stereotype.Service;

@Service
public class ClosePaymentOrderService implements ClosePaymentOrderUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentOrderRepository paymentOrderRepository;

    public ClosePaymentOrderService(
            PaymentProviderPortFactory portFactory,
            PaymentOrderRepository paymentOrderRepository
    ) {
        this.portFactory = portFactory;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Override
    public void execute(PaymentProvider provider, String providerOrderId, PaymentStatus status) {
        PaymentResult result = portFactory.gateway(provider).closePaymentOrder(providerOrderId, status);

        paymentOrderRepository.findByProviderOrderIdAndProvider(providerOrderId, provider)
                .ifPresent(existing -> paymentOrderRepository.save(
                        existing.withStatus(result.status()).withProviderPaymentId(result.providerPaymentId())
                ));
    }
}
