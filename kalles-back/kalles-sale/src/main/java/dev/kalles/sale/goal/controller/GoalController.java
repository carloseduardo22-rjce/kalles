package dev.kalles.sale.goal.controller;

import dev.kalles.sale.goal.dto.GoalRequest;
import dev.kalles.sale.goal.dto.GoalResponse;
import dev.kalles.sale.goal.service.GoalAssessmentResult;
import dev.kalles.sale.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/goals")
@Validated
@RequiredArgsConstructor
@Tag(name = "Metas de Faturamento", description = "Gestão de metas globais de faturamento por período")
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    @Operation(summary = "Listar metas", description = "Retorna todas as metas de faturamento cadastradas.")
    public ResponseEntity<List<GoalResponse>> listAll() {
        return ResponseEntity.ok(goalService.listAll());
    }

    @PostMapping
    @Operation(summary = "Criar meta", description = "Registra uma nova meta de faturamento com status DRAFT.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Meta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "422", description = "Conflito de período com meta ativa existente")
    })
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta encontrada"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<GoalResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(goalService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta", description = "Atualiza o valor alvo e o período de uma meta em rascunho.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta atualizada"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
        @ApiResponse(responseCode = "422", description = "Meta ativa não pode ser alterada")
    })
    public ResponseEntity<GoalResponse> update(@PathVariable UUID id, @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar meta", description = "Ativa uma meta em rascunho. Valida sobreposição de período com metas já ativas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta ativada"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
        @ApiResponse(responseCode = "422", description = "Conflito de período com meta ativa existente")
    })
    public ResponseEntity<GoalResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(goalService.activate(id));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Encerrar meta", description = "Encerra uma meta ativa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta encerrada"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<GoalResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(goalService.close(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir meta", description = "Remove uma meta de faturamento.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Meta excluída"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/assessment")
    @Operation(summary = "Apurar meta", description = "Calcula o valor realizado e a lacuna em relação ao valor alvo da meta.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Apuração calculada"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<GoalAssessmentResult> assess(
            @PathVariable UUID id,
            @RequestParam @Positive BigDecimal totalSold) {
        return ResponseEntity.ok(goalService.assess(id, totalSold));
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Progresso automático da meta", description = "Calcula automaticamente o valor realizado com base nas vendas do período.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Progresso calculado"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<GoalAssessmentResult> getProgress(@PathVariable UUID id) {
        return ResponseEntity.ok(goalService.getProgress(id));
    }
}
