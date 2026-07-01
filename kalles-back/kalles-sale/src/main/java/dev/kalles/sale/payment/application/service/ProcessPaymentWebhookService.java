package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.service.PaymentService;
import dev.kalles.sale.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.sale.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.sale.payment.application.port.out.PaymentWebhookEventObserver;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStatus;
import dev.kalles.sale.payment.domain.PaymentWebhookEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ProcessPaymentWebhookService implements ProcessPaymentWebhookUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentService paymentService;
    private final List<PaymentWebhookEventObserver> observers;

    public ProcessPaymentWebhookService(
            PaymentProviderPortFactory portFactory,
            PaymentOrderRepository paymentOrderRepository,
            PaymentService paymentService,
            List<PaymentWebhookEventObserver> observers
    ) {
        this.portFactory = portFactory;
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentService = paymentService;
        this.observers = observers;
    }

    @Override
    public boolean validateSignature(PaymentProvider provider, String xSignature, String xRequestId, String dataId) {
        return portFactory.webhook(provider).validateSignature(xSignature, xRequestId, dataId);
    }

    @Override
    public boolean execute(PaymentProvider provider, Map<String, Object> payload) {
        PaymentWebhookEvent event = portFactory.webhook(provider).parseEvent(payload);
        if (event == null) {
            return false;
        }

        if (event.providerOrderId() != null) {
            paymentOrderRepository.findByProviderOrderIdAndProvider(event.providerOrderId(), provider)
                    .ifPresent(existing -> paymentOrderRepository.save(
                            existing.withStatus(event.status()).withProviderPaymentId(event.providerPaymentId())
                    ));
        }

        if (isApproved(event.status()) && hasValidPaymentPayload(event.externalReference(), event.amount())) {
            try {
                paymentService.addPayment(
                        event.externalReference(),
                        toCorePaymentMethod(event.methodType()),
                        event.amount()
                );
            } catch (Exception e) {
                System.err.println("Erro ao processar pagamento vindo do webhook do provider " + provider + ": " + e.getMessage());
            }
        }

        observers.forEach(observer -> observer.onEvent(event));

        return true;
    }

    private boolean isApproved(PaymentStatus status) {
        return status == PaymentStatus.APPROVED;
    }

    private boolean hasValidPaymentPayload(String externalReference, BigDecimal amount) {
        return externalReference != null
                && !externalReference.isBlank()
                && amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private PaymentMethod toCorePaymentMethod(PaymentMethodType methodType) {
        if (methodType == null) {
            return PaymentMethod.PIX;
        }

        return switch (methodType) {
            case CREDIT_CARD, VOUCHER_CARD -> PaymentMethod.CREDIT_CARD;
            case DEBIT_CARD -> PaymentMethod.DEBIT_CARD;
            case UNSPECIFIED -> PaymentMethod.PIX;
        };
    }
}
