package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.StockRequest;
import dev.kalles.sale.core.dto.StockResponse;
import dev.kalles.sale.core.entity.CompanyProduct;
import dev.kalles.sale.core.entity.Location;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Stock;
import dev.kalles.sale.core.entity.StockEntry;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.CompanyProductRepository;
import dev.kalles.sale.core.repository.LocationRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.StockEntryRepository;
import dev.kalles.sale.core.repository.StockRepository;
import dev.kalles.sale.security.context.CompanyContextHolder;
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
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + request.productId()));
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new NotFoundException("Localizacao nao encontrada: " + request.locationId()));

        UUID companyId = getCompanyId();
        Stock stock = stockRepository
                .findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> new Stock(product, location, 0));

        int previousQuantity = stock.getQuantity();
        int quantityAdded = request.quantity() - previousQuantity;

        if (quantityAdded > 0) {
            BigDecimal unitCost = validateUnitCost(request.unitCost());
            registerStockEntry(companyId, product, location, quantityAdded, unitCost);
            updateProductCostPrice(companyId, product, unitCost);
        }

        stock.setQuantity(request.quantity());
        return StockResponse.from(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Produto nao encontrado: " + productId);
        }
        return stockRepository.findAllByProductIdOrderByQuantityDesc(productId, getCompanyId())
                .stream()
                .map(StockResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getTotalStockByProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Produto nao encontrado: " + productId);
        }
        return stockRepository.sumQuantityByProductId(productId, getCompanyId());
    }

    private UUID getCompanyId() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Nenhuma filial selecionada no contexto da operacao.");
        }
        return companyId;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByLocation(UUID locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException("Localizacao nao encontrada: " + locationId);
        }
        return stockRepository.findAllByLocationId(locationId)
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

    private void updateProductCostPrice(UUID companyId, Product product, BigDecimal unitCost) {
        companyProductRepository.findByCompanyIdAndProductId(companyId, product.getId())
                .ifPresent(companyProduct -> {
                    companyProduct.setCostPrice(unitCost);
                    companyProductRepository.save(companyProduct);
                });
    }
}
