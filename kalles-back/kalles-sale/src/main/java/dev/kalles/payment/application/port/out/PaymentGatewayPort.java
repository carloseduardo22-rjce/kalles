package dev.kalles.payment.application.port.out;

import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;

public interface PaymentGatewayPort extends ProviderAwarePort {

    PaymentResult processPayment(PaymentCommand command);

    PaymentResult getPayment(String providerOrderId);

    void cancelPayment(String providerOrderId);

    PaymentResult closePaymentOrder(String providerOrderId, PaymentStatus status);

    void printDocument(String providerOrderId, PaymentDocumentPrintCommand command);

    void refundPayment(String providerPaymentId);
}
