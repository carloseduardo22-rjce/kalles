package dev.kalles.sale.core.service;

import dev.kalles.sale.core.dto.StockRequest;
import dev.kalles.sale.core.dto.StockResponse;
import dev.kalles.sale.core.entity.Location;
import dev.kalles.sale.core.entity.Product;
import dev.kalles.sale.core.entity.Stock;
import dev.kalles.sale.core.entity.Warehouse;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.core.repository.LocationRepository;
import dev.kalles.sale.core.repository.ProductRepository;
import dev.kalles.sale.core.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService - Serviço de Estoque")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private StockService stockService;

    private Product buildProduct(UUID id) {
        Product p = new Product();
        p.setId(id);
        p.setName("Produto Teste");
        p.setInternalCode("PRD-001");
        // Product no longer has price or active at this level
        return p;
    }

    private Location buildLocation(UUID id) {
        Warehouse wh = new Warehouse(UUID.randomUUID(), "Dep A", null, true, UUID.randomUUID());
        return new Location(id, wh, "EST-01", null);
    }

    @Test
    @DisplayName("Deve criar nova entrada de estoque quando não existe")
    void shouldCreateNewStockEntryWhenNoneExists() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Location location = buildLocation(locationId);
        StockRequest request = new StockRequest(productId, locationId, 50);
        Stock saved = new Stock(UUID.randomUUID(), null, product, location, 50);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductIdAndLocationId(productId, locationId)).thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenReturn(saved);

        StockResponse response = stockService.setStock(request);

        assertNotNull(response);
        assertEquals(50, response.quantity());
        assertEquals(productId, response.productId());
        assertEquals(locationId, response.locationId());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve atualizar quantidade de entrada de estoque existente")
    void shouldUpdateQuantityOfExistingStockEntry() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Location location = buildLocation(locationId);
        StockRequest request = new StockRequest(productId, locationId, 80);
        Stock existing = new Stock(UUID.randomUUID(), null, product, location, 30);
        Stock updated = new Stock(existing.getId(), null, product, location, 80);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductIdAndLocationId(productId, locationId)).thenReturn(Optional.of(existing));
        when(stockRepository.save(existing)).thenReturn(updated);

        StockResponse response = stockService.setStock(request);

        assertEquals(80, response.quantity());
        verify(stockRepository).save(existing);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado ao definir estoque")
    void shouldThrowNotFoundWhenProductNotFoundOnSetStock() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.setStock(new StockRequest(productId, UUID.randomUUID(), 10)));
        verifyNoInteractions(locationRepository, stockRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando localização não encontrada ao definir estoque")
    void shouldThrowNotFoundWhenLocationNotFoundOnSetStock() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(buildProduct(productId)));
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.setStock(new StockRequest(productId, locationId, 10)));
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("Deve retornar todas as entradas de estoque para um produto")
    void shouldReturnAllStockEntriesForProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Stock s1 = new Stock(UUID.randomUUID(), null, product, buildLocation(UUID.randomUUID()), 30);
        Stock s2 = new Stock(UUID.randomUUID(), null, product, buildLocation(UUID.randomUUID()), 20);

        when(productRepository.existsById(productId)).thenReturn(true);
        // Mocking with ANY companyId since getCompanyId() will be called internally
        when(stockRepository.findAllByProductIdOrderByQuantityDesc(eq(productId), any())).thenReturn(List.of(s1, s2));

        List<StockResponse> result = stockService.getStockByProduct(productId);

        assertEquals(2, result.size());
        assertEquals(30, result.get(0).quantity());
        assertEquals(20, result.get(1).quantity());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado ao consultar estoque")
    void shouldThrowNotFoundWhenProductNotFoundOnGetStockByProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> stockService.getStockByProduct(productId));
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("Deve retornar o total de estoque para o produto")
    void shouldReturnTotalStockSumForProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(stockRepository.sumQuantityByProductId(eq(productId), any())).thenReturn(150);

        int total = stockService.getTotalStockByProduct(productId);

        assertEquals(150, total);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado ao consultar total de estoque")
    void shouldThrowNotFoundWhenProductNotFoundOnGetTotalStock() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> stockService.getTotalStockByProduct(productId));
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("Deve retornar todas as entradas de estoque para uma localização")
    void shouldReturnAllStockEntriesForLocation() {
        UUID locationId = UUID.randomUUID();
        Location location = buildLocation(locationId);
        Product product = buildProduct(UUID.randomUUID());
        Stock stock = new Stock(UUID.randomUUID(), null, product, location, 10);

        when(locationRepository.existsById(locationId)).thenReturn(true);
        when(stockRepository.findAllByLocationId(locationId)).thenReturn(List.of(stock));

        List<StockResponse> result = stockService.getStockByLocation(locationId);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).quantity());
    }

    @Test
    @DisplayName("Deve lançar exceção quando localização não encontrada ao consultar estoque")
    void shouldThrowNotFoundWhenLocationNotFoundOnGetStockByLocation() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.existsById(locationId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> stockService.getStockByLocation(locationId));
        verifyNoInteractions(stockRepository);
    }
}
