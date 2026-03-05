package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.StockRequest;
import dev.kalles.sale.core.dto.StockResponse;
import dev.kalles.sale.core.entity.Location;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Stock;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.LocationRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public StockResponse setStock(StockRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + request.productId()));
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new NotFoundException("Localização não encontrada: " + request.locationId()));

        Stock stock = stockRepository
                .findByProductIdAndLocationId(product.getId(), location.getId())
                .orElseGet(() -> new Stock(product, location, 0));

        stock.setQuantity(request.quantity());
        return StockResponse.from(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Produto não encontrado: " + productId);
        }
        return stockRepository.findAllByProductIdOrderByQuantityDesc(productId)
                .stream()
                .map(StockResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getTotalStockByProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Produto não encontrado: " + productId);
        }
        return stockRepository.sumQuantityByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByLocation(UUID locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new NotFoundException("Localização não encontrada: " + locationId);
        }
        return stockRepository.findAllByLocationId(locationId)
                .stream()
                .map(StockResponse::from)
                .toList();
    }
}
