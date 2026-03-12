package dev.kalles.sale.api.warehouse;

import dev.kalles.sale.core.dto.LocationRequest;
import dev.kalles.sale.core.dto.LocationResponse;
import dev.kalles.sale.core.dto.WarehouseRequest;
import dev.kalles.sale.core.dto.WarehouseResponse;
import dev.kalles.sale.core.service.LocationService;
import dev.kalles.sale.core.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Depósitos", description = "Gerenciamento de depósitos e suas localizações internas")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "Listar depósitos ativos", description = "Retorna todos os depósitos ativos ordenados por nome.")
    public ResponseEntity<List<WarehouseResponse>> listAll() {
        return ResponseEntity.ok(warehouseService.listActiveWarehouses());
    }

    @PostMapping
    @Operation(summary = "Criar depósito", description = "Cadastra um novo depósito.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Depósito criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.createWarehouse(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar depósito por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Depósito encontrado"),
        @ApiResponse(responseCode = "404", description = "Depósito não encontrado")
    })
    public ResponseEntity<WarehouseResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(warehouseService.findWarehouseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar depósito", description = "Atualiza nome e endereço do depósito.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Depósito atualizado"),
        @ApiResponse(responseCode = "404", description = "Depósito não encontrado")
    })
    public ResponseEntity<WarehouseResponse> update(@PathVariable UUID id, @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar depósito", description = "Marca o depósito como inativo.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Depósito desativado"),
        @ApiResponse(responseCode = "404", description = "Depósito não encontrado")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        warehouseService.deactivateWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{warehouseId}/locations")
    @Operation(summary = "Listar localizações do depósito", description = "Retorna todas as localizações (estantes, seções) de um depósito.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localizações retornadas"),
        @ApiResponse(responseCode = "404", description = "Depósito não encontrado")
    })
    public ResponseEntity<List<LocationResponse>> listLocations(@PathVariable UUID warehouseId) {
        return ResponseEntity.ok(locationService.listByWarehouse(warehouseId));
    }

    @PostMapping("/{warehouseId}/locations")
    @Operation(summary = "Criar localização", description = "Cadastra uma nova localização (estante, seção) dentro do depósito.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Localização criada"),
        @ApiResponse(responseCode = "404", description = "Depósito não encontrado")
    })
    public ResponseEntity<LocationResponse> createLocation(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(warehouseId, request));
    }

    @PutMapping("/locations/{locationId}")
    @Operation(summary = "Atualizar localização")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localização atualizada"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.update(locationId, request));
    }

    @DeleteMapping("/locations/{locationId}")
    @Operation(summary = "Excluir localização")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Localização excluída"),
        @ApiResponse(responseCode = "404", description = "Localização não encontrada")
    })
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID locationId) {
        locationService.delete(locationId);
        return ResponseEntity.noContent().build();
    }
}
