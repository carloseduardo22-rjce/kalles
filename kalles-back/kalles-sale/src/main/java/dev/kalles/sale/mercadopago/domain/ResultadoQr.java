package dev.kalles.sale.mercadopago.domain;

public record ResultadoQr(
        String orderId,
        String qrData
) {}
