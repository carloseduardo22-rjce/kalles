package dev.kalles.payment.adapter.out.stone;

import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentStatus;

final class StoneMappingUtils {

    private StoneMappingUtils() {
    }

    static PaymentStatus toPaymentStatus(String status) {
        if (status == null || status.isBlank()) {
            return PaymentStatus.UNKNOWN;
        }

        return switch (status.toLowerCase()) {
            case "pending", "waiting_payment" -> PaymentStatus.PENDING;
            case "processing" -> PaymentStatus.IN_PROGRESS;
            case "paid" -> PaymentStatus.APPROVED;
            case "canceled", "cancelled" -> PaymentStatus.CANCELED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "failed" -> PaymentStatus.FAILED;
            case "expired" -> PaymentStatus.EXPIRED;
            default -> PaymentStatus.UNKNOWN;
        };
    }

    static PaymentMethodType toPaymentMethodType(String paymentType, String fundingSource) {
        String normalizedFunding = fundingSource == null ? "" : fundingSource.toLowerCase();
        if (normalizedFunding.contains("credit")) {
            return PaymentMethodType.CREDIT_CARD;
        }
        if (normalizedFunding.contains("debit")) {
            return PaymentMethodType.DEBIT_CARD;
        }
        if (normalizedFunding.contains("prepaid")) {
            return PaymentMethodType.VOUCHER_CARD;
        }

        String normalizedType = paymentType == null ? "" : paymentType.toLowerCase();
        return switch (normalizedType) {
            case "credit" -> PaymentMethodType.CREDIT_CARD;
            case "debit" -> PaymentMethodType.DEBIT_CARD;
            case "voucher" -> PaymentMethodType.VOUCHER_CARD;
            default -> PaymentMethodType.UNSPECIFIED;
        };
    }

    static String toProviderCloseStatus(PaymentStatus status) {
        return switch (status) {
            case APPROVED -> "paid";
            case CANCELED -> "canceled";
            case FAILED -> "failed";
            default -> throw new IllegalArgumentException("Unsupported close status for payment order: " + status);
        };
    }

    static int toAmountInCents(java.math.BigDecimal amount) {
        return amount.movePointRight(2).intValueExact();
    }
}
