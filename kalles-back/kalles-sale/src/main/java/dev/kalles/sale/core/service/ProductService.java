package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.CompanyProductListItem;
import dev.kalles.sale.core.dto.ProductCatalogResponse;
import dev.kalles.sale.core.dto.ProductRequest;
import dev.kalles.sale.core.dto.ProductStockSummary;
import dev.kalles.sale.core.entity.CompanyProduct;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.CompanyProductReadRepository;
import dev.kalles.sale.core.repository.CompanyProductRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.ProductStockQueryRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
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
        UUID companyId = getCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.listCatalog(companyId, false);
        return enrichWithStock(companyId, catalog);
    }

    @Transactional(readOnly = true)
    public List<ProductCatalogResponse> listAll() {
        UUID companyId = getCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.listCatalog(companyId, true);
        return enrichWithStock(companyId, catalog);
    }

    @Transactional(readOnly = true)
    public Page<ProductCatalogResponse> listPage(boolean includeInactive, int page, int size) {
        UUID companyId = getCompanyId();
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
        UUID companyId = getCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.listCatalog(companyId, true);
        CompanyProductListItem item = catalog.stream()
                .filter(c -> c.productId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
        Map<UUID, Long> stockMap = buildStockMap(companyId, List.of(id));
        return ProductCatalogResponse.from(item, stockMap.getOrDefault(id, 0L));
    }

    @Transactional(readOnly = true)
    public List<ProductCatalogResponse> searchActive(String q) {
        UUID companyId = getCompanyId();
        List<CompanyProductListItem> catalog = companyProductReadRepository.searchActiveCatalog(companyId, q);
        return enrichWithStock(companyId, catalog);
    }

    @Transactional
    public ProductCatalogResponse create(ProductRequest request) {
        UUID tenantId = getTenantId();

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
        cp.setCompanyId(getCompanyId());
        cp.setProduct(product);
        cp.setPrice(request.price());
        cp.setCostPrice(request.costPrice());
        cp.setActive(true);
        companyProductRepository.save(cp);

        return findById(product.getId());
    }

    @Transactional
    public ProductCatalogResponse update(UUID id, ProductRequest request) {
        UUID tenantId = getTenantId();
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

        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(getCompanyId(), product.getId())
                .orElseGet(() -> {
                    CompanyProduct newCp = new CompanyProduct();
                    newCp.setCompanyId(getCompanyId());
                    newCp.setProduct(product);
                    newCp.setActive(true);
                    return newCp;
                });
        cp.setPrice(request.price());
        cp.setCostPrice(request.costPrice());
        companyProductRepository.save(cp);

        return findById(id);
    }

    @Transactional
    public void deactivate(UUID id) {
        CompanyProduct cp = companyProductRepository.findByCompanyIdAndProductId(getCompanyId(), id)
                .orElseThrow(() -> new NotFoundException("Produto nao configurado nesta filial."));
        cp.setActive(false);
        companyProductRepository.save(cp);
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Nenhum tenant selecionado no contexto da operacao.");
        }
        return tenantId;
    }

    private Product findTenantProduct(UUID productId) {
        return productRepository.findByIdAndTenantId(productId, getTenantId())
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
