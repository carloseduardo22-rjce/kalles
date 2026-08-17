package dev.kalles.payment.adapter.out.mercadopago.dto;

import java.util.List;

public record OrderTransactionsRequest(List<Payment> payments) {

    public record Payment(String amount) {
    }

    public static OrderTransactionsRequest ofSinglePayment(String amount) {
        return new OrderTransactionsRequest(List.of(new Payment(amount)));
    }
}
