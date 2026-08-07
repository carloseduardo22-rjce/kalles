package dev.kalles.cashregister.dto;

import dev.kalles.cashregister.entity.CashRegisterSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
    UUID sessionId,
    UUID operatorId,
    String cashRegisterCode,
    String operatorName,
    BigDecimal initialAmount,
    LocalDateTime openedAt,
    String status,
    boolean cashOnlyOperation
) {
    public static SessionResponse fromEntity(CashRegisterSession session) {
        return new SessionResponse(
            session.getId(),
            session.getOperator().getId(),
            session.getCashRegister().getCode(),
            session.getOperator().getName(),
            session.getInitialAmountValue(),
            session.getOpenedAt(),
            session.getStatus().name(),
            session.isCashOnlyOperation()
        );
    }
}
