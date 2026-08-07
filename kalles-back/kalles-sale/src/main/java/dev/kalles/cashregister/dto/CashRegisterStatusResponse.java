package dev.kalles.cashregister.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um caixa com o status da sessao atual.
 * Usado pela tela de gerenciamento de caixas do ADMIN.
 */
public record CashRegisterStatusResponse(
    UUID cashRegisterId,
    String code,
    String description,
    boolean active,
    boolean hasActiveSession,
    UUID activeSessionId,
    String activeOperatorName,
    BigDecimal initialAmount,
    LocalDateTime openedAt,
    boolean paymentIntegrationConfigured,
    Boolean activeSessionCashOnlyOperation
) {}
