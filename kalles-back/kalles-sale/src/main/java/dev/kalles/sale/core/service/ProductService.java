package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.ProductRequest;
import dev.kalles.sale.core.dto.ProductResponse;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.entity.CompanyProduct;
import dev.kalles.sale.core.repository.CompanyProductRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyProductRepository companyProductRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> listAllActive() {
        return productRepository.findAllActiveWithStock(getCompanyId());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return productRepository.findAllWithStock(getCompanyId());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return productRepository.findProductWithStockById(id, getCompanyId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchActive(String q) {
        return productRepository.searchActiveProductsWithStock(q, getCompanyId());
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operação.");
        }
        return companyId;
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
        product.setTenantId(TenantContextHolder.getTenantId());
        product.setName(request.name());
        product.setInternalCode(request.internalCode());
        product.setBarcode(request.barcode());
        product.setDescription(request.description());
        product = productRepository.save(product);

        CompanyProduct cp = new CompanyProduct();
        cp.setCompanyId(getCompanyId());
        cp.setProduct(product);
        cp.setPrice(request.price());
        cp.setActive(true);
        companyProductRepository.save(cp);

        return findById(product.getId());
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
        productRepository.save(product);

        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(getCompanyId(), product.getId())
                .orElseGet(() -> {
                    CompanyProduct newCp = new CompanyProduct();
                    newCp.setCompanyId(getCompanyId());
                    newCp.setProduct(product);
                    newCp.setActive(true);
                    return newCp;
                });
        cp.setPrice(request.price());
        companyProductRepository.save(cp);

        return findById(id);
    }

    @Transactional
    public void deactivate(UUID id) {
        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(getCompanyId(), id)
                .orElseThrow(() -> new NotFoundException("Produto não configurado nesta filial."));
        cp.setActive(false);
        companyProductRepository.save(cp);
    }
}
