package dev.kalles.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.enums.PaymentMethod;

public record SaleHistoryPaymentResponse(
        UUID id,
        UUID saleId,
        PaymentMethod method,
        BigDecimal amount,
        BigDecimal changeAmount,
        String transactionId,
        boolean confirmed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SaleHistoryPaymentResponse from(Payment payment) {
        return new SaleHistoryPaymentResponse(
                payment.getId(),
                payment.getSale().getId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getChangeAmount(),
                payment.getTransactionId(),
                payment.isConfirmed(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
