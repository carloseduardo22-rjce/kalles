package dev.kalles.api.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.kalles.sale.core.dto.ProductResponse;
import dev.kalles.sale.core.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Consulta de produtos")
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    @Operation(
        summary = "Listar todos os produtos ativos",
        description = "Retorna todos os produtos com active=true, ordenados por nome."
    )
    public ResponseEntity<List<ProductResponse>> listAll() {
        return ResponseEntity.ok(
            productRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductResponse::from)
                .toList()
        );
    }

    @GetMapping("/search")
    @Operation(
        summary = "Buscar produtos",
        description = "Busca produtos ativos por nome, código interno ou código de barras (case-insensitive, parcial)."
    )
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(
            productRepository.searchActiveProducts(q.trim())
                .stream()
                .map(ProductResponse::from)
                .toList()
        );
    }
}
