package dev.kalles.product.service;

import dev.kalles.inventory.dto.ProductStockSummary;
import dev.kalles.inventory.repository.ProductStockQueryRepository;
import dev.kalles.product.dto.CompanyProductListItem;
import dev.kalles.product.dto.ProductCatalogResponse;
import dev.kalles.product.dto.ProductRequest;
import dev.kalles.product.entity.CompanyProduct;
import dev.kalles.product.entity.Product;
import dev.kalles.product.repository.CompanyProductReadRepository;
import dev.kalles.product.repository.CompanyProductRepository;
import dev.kalles.product.repository.ProductRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyProductRepository companyProductRepository;
    private final CompanyProductReadRepository companyProductReadRepository;
    private final ProductStockQueryRepository productStockQueryRepository;

    @Transactional(readOnly = true)
    public List<ProductCatalogResponse> listAllActive() {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.listCatalog(companyId, false);
        return enrichWithStock(companyId, catalog);
    }

    @Transactional(readOnly = true)
    public List<ProductCatalogResponse> listAll() {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.listCatalog(companyId, true);
        return enrichWithStock(companyId, catalog);
    }

    @Transactional(readOnly = true)
    public Page<ProductCatalogResponse> listPage(boolean includeInactive, int page, int size) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        Page<CompanyProductListItem> catalogPage = companyProductReadRepository.listCatalogPage(
                companyId,
                includeInactive,
                PageRequest.of(page, size)
        );
        List<ProductCatalogResponse> content = enrichWithStock(companyId, catalogPage.getContent());
        return new PageImpl<>(content, catalogPage.getPageable(), catalogPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductCatalogResponse findById(UUID id) {
        findTenantProduct(id);
        UUID companyId = CompanyContextHolder.requireCompanyId();
        CompanyProductListItem item = companyProductReadRepository.findCatalogItem(companyId, id)
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
        Map<UUID, Long> stockMap = buildStockMap(companyId, List.of(id));
        return ProductCatalogResponse.from(item, stockMap.getOrDefault(id, 0L));
    }

    @Transactional(readOnly = true)
    public List<ProductCatalogResponse> searchActive(String q) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.searchActiveCatalog(companyId, q);
        return enrichWithStock(companyId, catalog);
    }

    @Transactional
    public ProductCatalogResponse create(ProductRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();

        productRepository.findByInternalCodeAndTenantId(request.internalCode(), tenantId).ifPresent(existing -> {
            throw new IllegalArgumentException("Ja existe um produto com o codigo interno informado.");
        });
        if (request.barcode() != null && !request.barcode().isBlank()) {
            productRepository.findByBarcodeAndTenantId(request.barcode(), tenantId).ifPresent(existing -> {
                throw new IllegalArgumentException("Ja existe um produto com o codigo de barras informado.");
            });
        }

        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName(request.name());
        product.setInternalCode(request.internalCode());
        product.setBarcode(request.barcode());
        product.setDescription(request.description());
        product = productRepository.save(product);

        CompanyProduct cp = new CompanyProduct();
        cp.setCompanyId(CompanyContextHolder.requireCompanyId());
        cp.setProduct(product);
        cp.setPrice(request.price());
        cp.setCostPrice(request.costPrice());
        cp.setActive(true);
        companyProductRepository.save(cp);

        return findById(product.getId());
    }

    @Transactional
    public ProductCatalogResponse update(UUID id, ProductRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Product product = findTenantProduct(id);

        productRepository.findByInternalCodeAndTenantId(request.internalCode(), tenantId).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Ja existe um produto com o codigo interno informado.");
            }
        });
        if (request.barcode() != null && !request.barcode().isBlank()) {
            productRepository.findByBarcodeAndTenantId(request.barcode(), tenantId).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Ja existe um produto com o codigo de barras informado.");
                }
            });
        }

        product.setName(request.name());
        product.setInternalCode(request.internalCode());
        product.setBarcode(request.barcode());
        product.setDescription(request.description());
        productRepository.save(product);

        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(CompanyContextHolder.requireCompanyId(), product.getId())
                .orElseThrow(() -> new NotFoundException("Produto nao configurado nesta filial."));
        cp.setPrice(request.price());
        cp.setCostPrice(request.costPrice());
        companyProductRepository.save(cp);

        return findById(id);
    }

    @Transactional
    public void deactivate(UUID id) {
        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(CompanyContextHolder.requireCompanyId(), id)
                .orElseThrow(() -> new NotFoundException("Produto nao configurado nesta filial."));
        cp.setActive(false);
        companyProductRepository.save(cp);
    }


    private Product findTenantProduct(UUID productId) {
        return productRepository.findByIdAndTenantId(productId, TenantContextHolder.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + productId));
    }

    private List<ProductCatalogResponse> enrichWithStock(UUID companyId, List<CompanyProductListItem> catalog) {
        if (catalog.isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> productIds = catalog.stream().map(CompanyProductListItem::productId).toList();
        Map<UUID, Long> stockMap = buildStockMap(companyId, productIds);
        return catalog.stream()
                .map(item -> ProductCatalogResponse.from(item, stockMap.getOrDefault(item.productId(), 0L)))
                .toList();
    }

    private Map<UUID, Long> buildStockMap(UUID companyId, List<UUID> productIds) {
        return productStockQueryRepository.summarizeByCompany(companyId, productIds)
                .stream()
                .collect(Collectors.toMap(ProductStockSummary::productId, ProductStockSummary::totalQuantity));
    }
}
