package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.core.enums.payment.PaymentMethod;
import dev.kalles.sale.core.service.PaymentService;
import dev.kalles.sale.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.sale.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.sale.payment.application.port.out.PaymentWebhookEventObserver;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentStatus;
import dev.kalles.sale.payment.domain.PaymentWebhookEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @Transactional
    public boolean execute(PaymentProvider provider, Map<String, Object> payload) {
        PaymentWebhookEvent event = portFactory.webhook(provider).parseEvent(payload);
        if (event == null) {
            return false;
        }

        // Idempotência: providers reenviam webhooks até receber 2xx. Se a ordem
        // já estava APPROVED, este evento é um reenvio e não deve gerar novo pagamento.
        boolean orderAlreadyApproved = false;
        if (event.providerOrderId() != null) {
            Optional<PaymentOrder> existing =
                    paymentOrderRepository.findByProviderOrderIdAndProvider(event.providerOrderId(), provider);
            orderAlreadyApproved = existing
                    .map(order -> order.status() == PaymentStatus.APPROVED)
                    .orElse(false);
            existing.ifPresent(order -> paymentOrderRepository.save(
                    order.withStatus(event.status()).withProviderPaymentId(event.providerPaymentId())
            ));
        }

        if (isApproved(event.status())
                && !orderAlreadyApproved
                && hasValidPaymentPayload(event.externalReference(), event.amount())) {
            // Sem try/catch: falha aqui reverte a transação (inclusive o status da
            // ordem) e devolve erro ao provider, que reenviará o webhook. Engolir a
            // exceção descartaria um pagamento já capturado no cartão do cliente.
            paymentService.registerExternalPayment(
                    event.externalReference(),
                    toCorePaymentMethod(event.methodType()),
                    event.amount(),
                    event.providerPaymentId()
            );
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
            return PaymentMethod.OTHER;
        }

        return switch (methodType) {
            case CREDIT_CARD -> PaymentMethod.CREDIT_CARD;
            case DEBIT_CARD -> PaymentMethod.DEBIT_CARD;
            // Método não informado pelo provider não pode ser assumido como PIX:
            // classificá-lo errado distorce os totais por método no fechamento de caixa.
            case VOUCHER_CARD, UNSPECIFIED -> PaymentMethod.OTHER;
        };
    }
}
