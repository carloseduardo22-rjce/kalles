package dev.kalles.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import dev.kalles.sale.entity.Payment;
import dev.kalles.sale.enums.PaymentMethod;

public record PaymentResponse(
    UUID id,
    PaymentMethod method,
    BigDecimal amount,
    BigDecimal changeAmount,
    boolean confirmed,
    String transactionId,
    LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getMethod(),
            payment.getAmount(),
            payment.getChangeAmount(),
            payment.isConfirmed(),
            payment.getTransactionId(),
            payment.getCreatedAt()
        );
    }
}
