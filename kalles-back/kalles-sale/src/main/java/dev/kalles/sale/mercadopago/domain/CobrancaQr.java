package dev.kalles.sale.mercadopago.domain;

import java.math.BigDecimal;

public record CobrancaQr(
        String orderIdErp,
        BigDecimal amount,
        String caixaExternalId,
        String idempotencyKey
) {}
