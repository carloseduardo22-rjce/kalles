package dev.kalles.sale.cashregister.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

public record SessionSummaryResponse(

        @Schema(description = "Total de vendas concluídas na sessão")
        int vendasConcluidas,

        @Schema(description = "Total de vendas canceladas na sessão")
        int vendasCanceladas,

        @Schema(description = "Soma dos totais de todas as vendas concluídas")
        BigDecimal totalVendido,

        @Schema(description = "Valor arrecadado por método de pagamento (ex: CASH, PIX, CREDIT_CARD, DEBIT_CARD)")
        Map<String, BigDecimal> totalPorMetodoPagamento

) {}
