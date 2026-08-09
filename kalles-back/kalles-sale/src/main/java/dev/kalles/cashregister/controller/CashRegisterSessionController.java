package dev.kalles.cashregister.controller;

import dev.kalles.cashregister.dto.CloseSessionResponse;
import dev.kalles.cashregister.dto.CloseSessionRequest;
import dev.kalles.cashregister.dto.OpenSessionRequest;
import dev.kalles.cashregister.dto.SessionResponse;
import dev.kalles.cashregister.dto.SessionSummaryResponse;
import dev.kalles.cashregister.service.CloseSessionUseCase;
import dev.kalles.cashregister.service.OpenSessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/cash-register-sessions")
@RequiredArgsConstructor
@Tag(name = "Sessões de Caixa", description = "Abertura, fechamento e relatório das sessões de caixa")
public class CashRegisterSessionController {

    private final OpenSessionUseCase openSessionUseCase;
    private final CloseSessionUseCase closeSessionUseCase;

    @PostMapping("/open")
    @Operation(summary = "Abrir sessão de caixa",
            description = "Registra a abertura de um caixa com o operador e o valor inicial em espécie.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sessão aberta com sucesso"),
        @ApiResponse(responseCode = "404", description = "Caixa ou operador não encontrado", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "Já existe uma sessão ativa para este caixa", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<SessionResponse> openSession(@Valid @RequestBody OpenSessionRequest request) {
        SessionResponse response = openSessionUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{sessionId}/close")
    @Operation(summary = "Fechar sessão de caixa",
            description = "Fecha a sessão ativa do caixa e retorna o resumo financeiro do dia: total vendido, vendas concluídas/canceladas e breakdown por método de pagamento.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessão fechada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "Sessão já está fechada", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<CloseSessionResponse> closeSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CloseSessionRequest request
    ) {
        CloseSessionResponse response = closeSessionUseCase.execute(sessionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Consultar detalhes da sessao",
            description = "Retorna os dados completos da sessao e, quando houver, o fechamento persistido.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessao retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessao nao encontrada", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<CloseSessionResponse> getSessionDetails(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(closeSessionUseCase.getSessionDetails(sessionId));
    }

    @GetMapping("/{sessionId}/report")
    @Operation(summary = "Consultar resumo da sessão",
            description = "Retorna o resumo de vendas de uma sessão (disponível tanto com a sessão aberta quanto após o fechamento). Útil para conferência durante o turno.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<SessionSummaryResponse> getReport(@PathVariable UUID sessionId) {
        SessionSummaryResponse response = closeSessionUseCase.getReport(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar sessões por intervalo de datas",
            description = "Retorna todas as sessões de caixa (abertas ou fechadas) cujas aberturas ocorreram entre startDate e endDate (inclusive). Usado para o histórico de sessões.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessões retornadas com sucesso")
    })
    public ResponseEntity<List<CloseSessionResponse>> listSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(closeSessionUseCase.listSessionsByDateRange(startDate, endDate));
    }
}
