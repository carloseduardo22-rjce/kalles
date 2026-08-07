package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.in.PrintPaymentDocumentUseCase;
import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentProvider;
import org.springframework.stereotype.Service;

@Service
public class PrintPaymentDocumentService implements PrintPaymentDocumentUseCase {

    private final PaymentProviderPortFactory portFactory;

    public PrintPaymentDocumentService(PaymentProviderPortFactory portFactory) {
        this.portFactory = portFactory;
    }

    @Override
    public void execute(PaymentProvider provider, String providerOrderId, PaymentDocumentPrintCommand command) {
        portFactory.gateway(provider).printDocument(providerOrderId, command);
    }
}
