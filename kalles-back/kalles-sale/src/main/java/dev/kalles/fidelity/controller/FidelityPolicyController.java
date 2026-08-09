package dev.kalles.fidelity.controller;

import dev.kalles.fidelity.dto.FidelityPolicyRequest;
import dev.kalles.fidelity.dto.FidelityPolicyResponse;
import dev.kalles.fidelity.service.FidelityPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/fidelity-policies")
@Validated
@RequiredArgsConstructor
@Tag(name = "Políticas de Fidelidade", description = "Gerenciamento das regras do programa de fidelidade")
public class FidelityPolicyController {

    private final FidelityPolicyService fidelityPolicyService;

    @PostMapping
    @Operation(summary = "Criar política de fidelidade",
            description = "Cria uma nova política de fidelidade e desativa automaticamente a política anterior. Apenas uma política pode estar ativa por vez.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Política criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<FidelityPolicyResponse> create(
            @Valid @RequestBody FidelityPolicyRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(fidelityPolicyService.create(request));
    }

    @GetMapping("/active")
    @Operation(summary = "Consultar política de fidelidade ativa",
            description = "Retorna a política de fidelidade atualmente ativa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Política ativa retornada"),
        @ApiResponse(responseCode = "404", description = "Nenhuma política ativa encontrada")
    })
    public ResponseEntity<FidelityPolicyResponse> getActive() {
        return ResponseEntity.ok(fidelityPolicyService.getActive());
    }

    @GetMapping
    @Operation(summary = "Listar todas as políticas",
            description = "Retorna o histórico de todas as políticas de fidelidade criadas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    public ResponseEntity<List<FidelityPolicyResponse>> listAll() {
        return ResponseEntity.ok(fidelityPolicyService.listAll());
    }
}
