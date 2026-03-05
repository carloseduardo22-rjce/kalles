package dev.kalles.api.operator;

import dev.kalles.sale.cashregister.dto.OperatorRequest;
import dev.kalles.sale.cashregister.dto.OperatorResponse;
import dev.kalles.sale.cashregister.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
@Tag(name = "Operadores", description = "Cadastro e gestão de operadores")
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping
    @Operation(summary = "Listar operadores ativos", description = "Retorna todos os operadores ativos ordenados por nome.")
    public ResponseEntity<List<OperatorResponse>> listAll() {
        return ResponseEntity.ok(operatorService.listAll());
    }

    @PostMapping
    @Operation(summary = "Cadastrar operador")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Operador cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou código já cadastrado")
    })
    public ResponseEntity<OperatorResponse> create(@Valid @RequestBody OperatorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar operador por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operador encontrado"),
        @ApiResponse(responseCode = "404", description = "Operador não encontrado")
    })
    public ResponseEntity<OperatorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(operatorService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar operador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operador atualizado"),
        @ApiResponse(responseCode = "404", description = "Operador não encontrado"),
        @ApiResponse(responseCode = "400", description = "Código já utilizado por outro operador")
    })
    public ResponseEntity<OperatorResponse> update(
            @PathVariable UUID id, @Valid @RequestBody OperatorRequest request) {
        return ResponseEntity.ok(operatorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar operador", description = "Marca o operador como inativo (soft delete).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Operador desativado"),
        @ApiResponse(responseCode = "404", description = "Operador não encontrado")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        operatorService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
