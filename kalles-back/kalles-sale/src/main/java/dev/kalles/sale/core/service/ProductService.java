package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.ProductRequest;
import dev.kalles.sale.core.dto.ProductResponse;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> listAllActive() {
        return productRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return productRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchActive(String q) {
        return productRepository.searchActiveProducts(q)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        productRepository.findByInternalCode(request.internalCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe um produto com o código interno informado.");
        });
        if (request.barcode() != null && !request.barcode().isBlank()) {
            productRepository.findByBarcode(request.barcode()).ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe um produto com o código de barras informado.");
            });
        }
        Product product = new Product();
        product.setName(request.name());
        product.setInternalCode(request.internalCode());
        product.setBarcode(request.barcode());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setActive(true);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));

        productRepository.findByInternalCode(request.internalCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Já existe um produto com o código interno informado.");
            }
        });
        if (request.barcode() != null && !request.barcode().isBlank()) {
            productRepository.findByBarcode(request.barcode()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe um produto com o código de barras informado.");
                }
            });
        }

        product.setName(request.name());
        product.setInternalCode(request.internalCode());
        product.setBarcode(request.barcode());
        product.setDescription(request.description());
        product.setPrice(request.price());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));
        product.setActive(false);
        productRepository.save(product);
    }
}
