package dev.kalles.sale.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

import java.math.BigDecimal;

public class DiscountExceedsItemTotalException extends BusinessRuleViolationException {

    public DiscountExceedsItemTotalException(BigDecimal itemTotal) {
        super("DISCOUNT_EXCEEDS_ITEM_TOTAL", "O desconto não pode exceder o valor do produto. Valor do item: R$ " + itemTotal);
    }
}
