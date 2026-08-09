package dev.kalles.cashregister.dto;

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
        Map<String, BigDecimal> totalPorMetodoPagamento,

        @Schema(description = "Total confirmado em pagamentos em dinheiro")
        BigDecimal totalEmDinheiro,

        @Schema(description = "Saldo esperado em dinheiro no caixa: fundo inicial + pagamentos em dinheiro")
        BigDecimal saldoEsperadoEmCaixa,

        @Schema(description = "Valor contado em dinheiro informado no fechamento. Nulo enquanto a sessao estiver aberta")
        BigDecimal valorInformadoEmCaixa,

        @Schema(description = "Diferenca entre o valor contado e o saldo esperado. Nulo enquanto a sessao estiver aberta")
        BigDecimal diferencaEmCaixa

) {}
