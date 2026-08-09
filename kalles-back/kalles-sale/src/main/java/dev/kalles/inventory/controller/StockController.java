package dev.kalles.inventory.controller;

import dev.kalles.inventory.dto.StockRequest;
import dev.kalles.inventory.dto.StockResponse;
import dev.kalles.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Gerenciamento de quantidades de produtos por localização")
public class StockController {

    private final StockService stockService;

    @PostMapping
    @Operation(
        summary = "Definir estoque",
        description = "Define (ou atualiza) a quantidade de um produto em uma localização específica. " +
                      "Se já existir um registro para esse produto e localização, ele é atualizado; caso contrário, um novo é criado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estoque definido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto ou localização não encontrada")
    })
    public ResponseEntity<StockResponse> setStock(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(stockService.setStock(request));
    }

    @GetMapping("/product/{productId}")
    @Operation(
        summary = "Consultar estoque por produto",
        description = "Retorna todos os registros de estoque de um produto, listados por localização, " +
                      "ordenados da maior para a menor quantidade."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registros retornados"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<List<StockResponse>> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(stockService.getStockByProduct(productId));
    }

    @GetMapping("/product/{productId}/total")
    @Operation(
        summary = "Total de estoque por produto",
        description = "Retorna a soma de todas as quantidades disponíveis para um produto em todos os depósitos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total retornado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Integer> getTotalByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(stockService.getTotalStockByProduct(productId));
    }

    @GetMapping("/location/{locationId}")
    @Operation(
        summary = "Consultar estoque por localização",
        description = "Retorna todos os registros de estoque de uma localização (estante ou seção específica)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registros retornados"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public ResponseEntity<List<StockResponse>> getByLocation(@PathVariable UUID locationId) {
        return ResponseEntity.ok(stockService.getStockByLocation(locationId));
    }
}
