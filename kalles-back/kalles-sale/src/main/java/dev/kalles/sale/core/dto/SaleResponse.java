package dev.kalles.sale.core.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import dev.kalles.sale.core.entity.Sale;
import io.swagger.v3.oas.annotations.media.Schema;

public record SaleResponse(
    UUID id,
    String sessionToken,
    @Schema(description = "Estado atual da venda", allowableValues = {"OPEN", "ON_HOLD", "PAYMENT_IN_PROGRESS", "PAID", "COMPLETED", "CANCELED"})
    String state,
    List<SaleItemResponse> items,
    List<PaymentResponse> payments,
    @Schema(description = "Subtotal sem descontos")
    BigDecimal subtotal,
    @Schema(description = "Total após descontos aplicados")
    BigDecimal total,
    @Schema(description = "Valor ainda a ser pago. Chega a zero quando o pagamento é completo.")
    BigDecimal amountDue
) {
    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
            sale.getId(),
            sale.getSessionToken(),
            sale.getStateName(),
            sale.getItems().stream().map(SaleItemResponse::from).toList(),
            sale.getPayments().stream().map(PaymentResponse::from).toList(),
            sale.getSubtotal(),
            sale.getTotal(),
            sale.getAmountDue()
        );
    }
}
