package dev.kalles.sale.mercadopago.domain;

import java.math.BigDecimal;

public record CobrancaPoint(
        String orderIdErp,
        BigDecimal amount,
        String terminalId,
        String idempotencyKey,
        String description,
        String paymentMethodType
) {}
