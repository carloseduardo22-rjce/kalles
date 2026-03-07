package dev.kalles.api.fidelity;

import dev.kalles.sale.core.dto.FidelityResponse;
import dev.kalles.sale.core.service.FidelityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/fidelity")
@Validated
@RequiredArgsConstructor
@Tag(name = "Fidelidade", description = "Operações do programa de fidelidade de clientes")
public class FidelityController {

    private final FidelityService fidelityService;

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Consultar fidelidade do cliente",
            description = "Retorna os dados do programa de fidelidade do cliente, incluindo pontos e desconto disponível.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados de fidelidade retornados"),
        @ApiResponse(responseCode = "404", description = "Cliente não está no programa de fidelidade")
    })
    public ResponseEntity<FidelityResponse> getByClientId(
            @PathVariable @NotNull UUID clientId) {

        return ResponseEntity.ok(fidelityService.getByClientId(clientId));
    }

    @PostMapping("/enroll/{clientId}")
    @Operation(summary = "Inscrever cliente no programa de fidelidade",
            description = "Cria uma carteira de fidelidade para o cliente. O cliente começa com zero pontos e sem desconto disponível.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cliente inscrito com sucesso"),
        @ApiResponse(responseCode = "400", description = "Cliente já está inscrito no programa"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<FidelityResponse> enroll(
            @PathVariable @NotNull UUID clientId) {

        return ResponseEntity.status(HttpStatus.CREATED).body(fidelityService.enrollClient(clientId));
    }
}
