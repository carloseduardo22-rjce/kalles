package dev.kalles.core.service;

import dev.kalles.core.dto.StockRequest;
import dev.kalles.core.dto.StockResponse;
import dev.kalles.core.entity.CompanyProduct;
import dev.kalles.core.entity.Location;
import dev.kalles.core.entity.Product;
import dev.kalles.core.entity.Stock;
import dev.kalles.core.entity.StockEntry;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.CompanyProductRepository;
import dev.kalles.core.repository.LocationRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.core.repository.StockEntryRepository;
import dev.kalles.core.repository.StockRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockEntryRepository stockEntryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final CompanyProductRepository companyProductRepository;

    @Transactional
    public StockResponse setStock(StockRequest request) {
        UUID companyId = getCompanyId();
        Product product = findTenantProduct(request.productId());
        CompanyProduct companyProduct = companyProductRepository.findByCompanyIdAndProductId(companyId, product.getId())
                .orElseThrow(() -> new NotFoundException("Produto nao configurado nesta filial: " + request.productId()));

        Location location = locationRepository.findByIdAndCompanyId(request.locationId(), companyId)
                .orElseThrow(() -> new NotFoundException("Localizacao nao encontrada ou nao pertence a esta empresa: " + request.locationId()));

        Stock stock = stockRepository
                .findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> new Stock(product, location, 0));

        int previousQuantity = stock.getQuantity();
        int quantityAdded = request.quantity() - previousQuantity;

        if (quantityAdded > 0) {
            BigDecimal unitCost = validateUnitCost(request.unitCost());
            registerStockEntry(companyId, product, location, quantityAdded, unitCost);
            updateProductCostPrice(companyProduct, unitCost);
        }

        stock.setQuantity(request.quantity());
        return StockResponse.from(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(UUID productId) {
        Product product = findTenantProduct(productId);
        return stockRepository.findAllByProductIdOrderByQuantityDesc(product.getId(), getCompanyId())
                .stream()
                .map(StockResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getTotalStockByProduct(UUID productId) {
        Product product = findTenantProduct(productId);
        return stockRepository.sumQuantityByProductId(product.getId(), getCompanyId());
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

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByLocation(UUID locationId) {
        UUID companyId = getCompanyId();
        locationRepository.findByIdAndCompanyId(locationId, companyId)
                .orElseThrow(() -> new NotFoundException("Localizacao nao encontrada ou nao pertence a esta empresa: " + locationId));
        return stockRepository.findAllByLocationIdAndCompanyId(locationId, companyId)
                .stream()
                .map(StockResponse::from)
                .toList();
    }

    private BigDecimal validateUnitCost(BigDecimal unitCost) {
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O custo da mercadoria e obrigatorio para registrar entradas no estoque.");
        }
        return unitCost;
    }

    private void registerStockEntry(UUID companyId, Product product, Location location, int quantityAdded, BigDecimal unitCost) {
        StockEntry stockEntry = new StockEntry();
        stockEntry.setCompanyId(companyId);
        stockEntry.setProduct(product);
        stockEntry.setLocation(location);
        stockEntry.setQuantityAdded(quantityAdded);
        stockEntry.setUnitCost(unitCost);
        stockEntry.setTotalCost(unitCost.multiply(BigDecimal.valueOf(quantityAdded)));
        stockEntryRepository.save(stockEntry);
    }

    private Product findTenantProduct(UUID productId) {
        return productRepository.findByIdAndTenantId(productId, getTenantId())
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + productId));
    }

    private void updateProductCostPrice(CompanyProduct companyProduct, BigDecimal unitCost) {
        companyProduct.setCostPrice(unitCost);
        companyProductRepository.save(companyProduct);
    }
}
