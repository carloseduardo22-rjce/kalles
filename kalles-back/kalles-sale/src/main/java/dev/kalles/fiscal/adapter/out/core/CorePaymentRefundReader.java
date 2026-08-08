package dev.kalles.fiscal.adapter.out.core;

import dev.kalles.fiscal.application.port.out.FiscalRefundReader;
import dev.kalles.sale.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CorePaymentRefundReader implements FiscalRefundReader {

    private final PaymentRepository paymentRepository;

    @Override
    public boolean hasConfirmedRefund(UUID tenantId, UUID companyId, UUID saleId) {
        return paymentRepository.findAllBySaleId(saleId).stream()
                .anyMatch(payment -> payment.isConfirmed()
                        && payment.getTransactionId() != null
                        && payment.getTransactionId().startsWith("refund:"));
    }
}
