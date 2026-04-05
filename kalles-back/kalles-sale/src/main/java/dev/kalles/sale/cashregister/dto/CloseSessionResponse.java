package dev.kalles.sale.cashregister.dto;

import dev.kalles.sale.cashregister.entity.CashRegisterSession;
import dev.kalles.sale.cashregister.entity.Operator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// CloseSessionResponse
public record CloseSessionResponse(

        UUID sessionId,
        String codigoCaixa,
        String nomeOperador,
        String codigoOperadorAutorizador,
        String nomeOperadorAutorizador,
        BigDecimal valorInicial,
        LocalDateTime abertura,
        LocalDateTime fechamento,

        @Schema(description = "Estado da sessão após o fechamento", example = "CLOSED")
        String status,

        @Schema(description = "Resumo financeiro da sessão")
        SessionSummaryResponse resumo

) {
    public static CloseSessionResponse fromEntity(CashRegisterSession session, SessionSummaryResponse resumo) {
        return fromEntity(session, resumo, null);
    }

    public static CloseSessionResponse fromEntity(
            CashRegisterSession session,
            SessionSummaryResponse resumo,
            Operator authorizedByOperator
    ) {
        return new CloseSessionResponse(
                session.getId(),
                session.getCashRegister().getCode(),
                session.getOperator().getName(),
                authorizedByOperator != null ? authorizedByOperator.getCode() : null,
                authorizedByOperator != null ? authorizedByOperator.getName() : null,
                session.getInitialAmountValue(),
                session.getOpenedAt(),
                session.getClosedAt(),
                session.getStatus().name(),
                resumo
        );
    }
}
