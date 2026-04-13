package dev.kalles.sale.api.product;

import dev.kalles.sale.api.dto.PageResponse;
import dev.kalles.sale.core.dto.ProductCatalogResponse;
import dev.kalles.sale.core.dto.ProductRequest;
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
    public ResponseEntity<List<ProductCatalogResponse>> listAll(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(
            includeInactive ? productService.listAll() : productService.listAllActive()
        );
    }

    @GetMapping("/page")
    @Operation(
        summary = "Listar produtos com paginação",
        description = "Retorna produtos paginados e ordenados por nome."
    )
    public ResponseEntity<PageResponse<ProductCatalogResponse>> listPage(
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(PageResponse.from(productService.listPage(includeInactive, page, size)));
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou código já cadastrado")
    })
    public ResponseEntity<ProductCatalogResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProductCatalogResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "400", description = "Código já utilizado por outro produto")
    })
    public ResponseEntity<ProductCatalogResponse> update(
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
    public ResponseEntity<List<ProductCatalogResponse>> search(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(productService.searchActive(q.trim()));
    }
}
