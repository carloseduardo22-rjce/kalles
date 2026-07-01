package dev.kalles.sale.core.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import dev.kalles.sale.core.entity.Sale;

public record SaleHistoryResponse(
        UUID id,
        Long version,
        String sessionToken,
        UUID companyId,
        String state,
        UUID clientId,
        BigDecimal subtotal,
        BigDecimal total,
        BigDecimal amountDue,
        BigDecimal fidelityDiscountApplied,
        Integer pointsEarned,
        LocalDateTime openedAt,
        List<SaleHistoryItemResponse> items,
        List<SaleHistoryPaymentResponse> payments
) {
    public static SaleHistoryResponse from(Sale sale, LocalDateTime openedAt) {
        return new SaleHistoryResponse(
                sale.getId(),
                sale.getVersion(),
                sale.getSessionToken(),
                sale.getCompanyId(),
                sale.getStateName(),
                sale.getClient() != null ? sale.getClient().getId() : null,
                sale.getSubtotal(),
                sale.getTotal(),
                sale.getAmountDue(),
                sale.getFidelityDiscountApplied(),
                sale.getPointsEarned(),
                openedAt,
                sale.getItems().stream()
                        .sorted(Comparator.comparing(item -> item.getId().toString()))
                        .map(SaleHistoryItemResponse::from)
                        .toList(),
                sale.getPayments().stream()
                        .sorted(Comparator.comparing(payment -> payment.getId().toString()))
                        .map(SaleHistoryPaymentResponse::from)
                        .toList()
        );
    }
}
