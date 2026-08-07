package dev.kalles.core.strategy;

public record PaymentResult(
    boolean confirmed,
    String transactionId,
    String message
) {

    public static PaymentResult confirmed(String message) {
        return new PaymentResult(true, null, message);
    }

    public static PaymentResult confirmed(String transactionId, String message) {
        return new PaymentResult(true, transactionId, message);
    }

    public static PaymentResult pending(String transactionId, String message) {
        return new PaymentResult(false, transactionId, message);
    }
}
