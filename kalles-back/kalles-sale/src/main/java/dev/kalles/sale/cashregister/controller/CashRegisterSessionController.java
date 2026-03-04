package dev.kalles.sale.cashregister.controller;

import dev.kalles.sale.cashregister.dto.CloseSessionResponse;
import dev.kalles.sale.cashregister.dto.OpenSessionRequest;
import dev.kalles.sale.cashregister.dto.SessionResponse;
import dev.kalles.sale.cashregister.dto.SessionSummaryResponse;
import dev.kalles.sale.cashregister.service.CloseSessionUseCase;
import dev.kalles.sale.cashregister.service.OpenSessionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
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
    public ResponseEntity<CloseSessionResponse> closeSession(@PathVariable UUID sessionId) {
        CloseSessionResponse response = closeSessionUseCase.execute(sessionId);
        return ResponseEntity.ok(response);
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
}
