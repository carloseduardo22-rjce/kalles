package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentStatus;
import dev.kalles.payment.domain.TerminalOperationMode;

public final class MercadoPagoMappingUtils {

    private MercadoPagoMappingUtils() {
    }

    public static PaymentStatus toPaymentStatus(String providerStatus) {
        if (providerStatus == null || providerStatus.isBlank()) {
            return PaymentStatus.UNKNOWN;
        }

        return switch (providerStatus.toLowerCase()) {
            case "created" -> PaymentStatus.CREATED;
            case "pending" -> PaymentStatus.PENDING;
            case "action_required" -> PaymentStatus.ACTION_REQUIRED;
            case "at_terminal", "opened" -> PaymentStatus.IN_PROGRESS;
            case "processed", "closed" -> PaymentStatus.APPROVED;
            case "canceled" -> PaymentStatus.CANCELED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "expired" -> PaymentStatus.EXPIRED;
            case "failed" -> PaymentStatus.FAILED;
            default -> PaymentStatus.UNKNOWN;
        };
    }

    public static String toProviderPaymentMethodType(PaymentMethodType methodType) {
        if (methodType == null) {
            return "credit_card";
        }

        return switch (methodType) {
            case DEBIT_CARD -> "debit_card";
            case CREDIT_CARD, VOUCHER_CARD, UNSPECIFIED -> "credit_card";
        };
    }

    public static PaymentMethodType toPaymentMethodType(String providerType) {
        if (providerType == null || providerType.isBlank()) {
            return PaymentMethodType.UNSPECIFIED;
        }

        return switch (providerType.toLowerCase()) {
            case "credit_card", "voucher_card" -> PaymentMethodType.CREDIT_CARD;
            case "debit_card" -> PaymentMethodType.DEBIT_CARD;
            default -> PaymentMethodType.UNSPECIFIED;
        };
    }

    public static TerminalOperationMode toTerminalOperationMode(String operationMode) {
        if (operationMode == null || operationMode.isBlank()) {
            return TerminalOperationMode.UNKNOWN;
        }

        return switch (operationMode.toUpperCase()) {
            case "PDV" -> TerminalOperationMode.POINT_OF_SALE;
            case "STANDALONE" -> TerminalOperationMode.STANDALONE;
            default -> TerminalOperationMode.UNKNOWN;
        };
    }

    public static String toProviderOperationMode(TerminalOperationMode operationMode) {
        if (operationMode == null) {
            return "PDV";
        }

        return switch (operationMode) {
            case POINT_OF_SALE -> "PDV";
            case STANDALONE -> "STANDALONE";
            case UNKNOWN -> "PDV";
        };
    }
}
