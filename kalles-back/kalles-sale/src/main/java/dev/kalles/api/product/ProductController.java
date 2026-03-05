package dev.kalles.api.product;

import dev.kalles.sale.core.dto.ProductRequest;
import dev.kalles.sale.core.dto.ProductResponse;
import dev.kalles.sale.core.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gestão do catálogo de produtos")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
        summary = "Listar produtos",
        description = "Retorna produtos ordenados por nome. Por padrão, retorna apenas ativos. Use includeInactive=true para retornar todos."
    )
    public ResponseEntity<List<ProductResponse>> listAll(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(
            includeInactive ? productService.listAll() : productService.listAllActive()
        );
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou código já cadastrado")
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "400", description = "Código já utilizado por outro produto")
    })
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar produto", description = "Marca o produto como inativo (soft delete).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produto desativado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
        summary = "Buscar produtos ativos",
        description = "Busca produtos ativos por nome, código interno ou código de barras (case-insensitive, parcial)."
    )
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(productService.searchActive(q.trim()));
    }
}
