package dev.kalles.sale.mercadopago.domain;

public record ResultadoPoint(
        String orderId,
        String status,
        String paymentId
) {}
