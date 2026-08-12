package dev.kalles.inventory.service;

import dev.kalles.inventory.dto.StockAdjustmentRequest;
import dev.kalles.inventory.dto.StockRequest;
import dev.kalles.inventory.dto.StockResponse;
import dev.kalles.inventory.entity.Location;
import dev.kalles.inventory.entity.Stock;
import dev.kalles.inventory.entity.StockAdjustment;
import dev.kalles.inventory.entity.StockEntry;
import dev.kalles.inventory.repository.LocationRepository;
import dev.kalles.inventory.repository.StockAdjustmentRepository;
import dev.kalles.inventory.repository.StockEntryRepository;
import dev.kalles.inventory.repository.StockRepository;
import dev.kalles.product.entity.CompanyProduct;
import dev.kalles.product.entity.Product;
import dev.kalles.product.repository.CompanyProductRepository;
import dev.kalles.product.repository.ProductRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.NotFoundException;
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
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final CompanyProductRepository companyProductRepository;

    @Transactional
    public StockResponse setStock(StockRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
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

    @Transactional
    public StockResponse adjustStock(StockAdjustmentRequest request) {
        UUID companyId = CompanyContextHolder.requireCompanyId();
        Product product = findTenantProduct(request.productId());
        companyProductRepository.findByCompanyIdAndProductId(companyId, product.getId())
                .orElseThrow(() -> new NotFoundException("Produto nao configurado nesta filial: " + request.productId()));

        Location location = locationRepository.findByIdAndCompanyId(request.locationId(), companyId)
                .orElseThrow(() -> new NotFoundException("Localizacao nao encontrada ou nao pertence a esta empresa: " + request.locationId()));

        Stock stock = stockRepository
                .findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> new Stock(product, location, 0));

        int previousQuantity = stock.getQuantity();
        stock.setQuantity(request.quantity());
        StockResponse response = StockResponse.from(stockRepository.save(stock));

        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setCompanyId(companyId);
        adjustment.setProduct(product);
        adjustment.setLocation(location);
        adjustment.setPreviousQuantity(previousQuantity);
        adjustment.setNewQuantity(request.quantity());
        adjustment.setReason(request.reason());
        stockAdjustmentRepository.save(adjustment);

        return response;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(UUID productId) {
        Product product = findTenantProduct(productId);
        return stockRepository.findAllByProductIdOrderByQuantityDesc(product.getId(), CompanyContextHolder.requireCompanyId())
                .stream()
                .map(StockResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getTotalStockByProduct(UUID productId) {
        Product product = findTenantProduct(productId);
        return stockRepository.sumQuantityByProductId(product.getId(), CompanyContextHolder.requireCompanyId());
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
        UUID companyId = CompanyContextHolder.requireCompanyId();
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
