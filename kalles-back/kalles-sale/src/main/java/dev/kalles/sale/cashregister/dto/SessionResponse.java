package dev.kalles.sale.cashregister.dto;

import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
    UUID sessionId,
    String cashRegisterCode,
    String operatorName,
    BigDecimal initialAmount,
    LocalDateTime openedAt,
    String status
) {
    public static SessionResponse fromEntity(CashRegisterSession session) {
        return new SessionResponse(
            session.getId(),
            session.getCashRegister().getCode(),
            session.getOperator().getName(),
            session.getInitialAmountValue(),
            session.getOpenedAt(),
            session.getStatus().name()
        );
    }
}
