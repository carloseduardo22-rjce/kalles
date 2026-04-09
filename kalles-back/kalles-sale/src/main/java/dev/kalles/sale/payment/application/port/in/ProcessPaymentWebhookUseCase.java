package dev.kalles.sale.payment.application.port.in;

import dev.kalles.sale.payment.domain.PaymentProvider;

import java.util.Map;

public interface ProcessPaymentWebhookUseCase {

    boolean validateSignature(PaymentProvider provider, String xSignature, String xRequestId, String dataId);

    boolean execute(PaymentProvider provider, Map<String, Object> payload);
}
