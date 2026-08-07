package dev.kalles.inventory.service;

import dev.kalles.core.entity.CompanyProduct;
import dev.kalles.core.entity.Product;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.CompanyProductRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.inventory.dto.StockRequest;
import dev.kalles.inventory.dto.StockResponse;
import dev.kalles.inventory.entity.Location;
import dev.kalles.inventory.entity.Stock;
import dev.kalles.inventory.entity.Warehouse;
import dev.kalles.inventory.repository.LocationRepository;
import dev.kalles.inventory.repository.StockEntryRepository;
import dev.kalles.inventory.repository.StockRepository;
import dev.kalles.inventory.service.StockService;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService - Servico de Estoque")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockEntryRepository stockEntryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CompanyProductRepository companyProductRepository;

    @InjectMocks
    private StockService stockService;

    private UUID companyId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        CompanyContextHolder.setCompanyId(companyId);
        TenantContextHolder.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        CompanyContextHolder.clear();
        TenantContextHolder.clear();
    }

    private Product buildProduct(UUID id) {
        Product p = new Product();
        p.setId(id);
        p.setTenantId(tenantId);
        p.setName("Produto Teste");
        p.setInternalCode("PRD-001");
        return p;
    }

    private Location buildLocation(UUID id) {
        Warehouse wh = new Warehouse(UUID.randomUUID(), "Dep A", companyId, "Endereco A", true);
        return new Location(id, wh, "EST-01", null);
    }

    @Test
    @DisplayName("Deve criar nova entrada de estoque quando nao existe")
    void shouldCreateNewStockEntryWhenNoneExists() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Location location = buildLocation(locationId);
        StockRequest request = new StockRequest(productId, locationId, 50, new BigDecimal("12.50"));
        Stock saved = new Stock(UUID.randomUUID(), null, product, location, 50);
        CompanyProduct companyProduct = new CompanyProduct();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(companyProductRepository.findByCompanyIdAndProductId(companyId, productId)).thenReturn(Optional.of(companyProduct));
        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductIdAndLocationId(productId, locationId)).thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenReturn(saved);

        StockResponse response = stockService.setStock(request);

        assertNotNull(response);
        assertEquals(50, response.quantity());
        assertEquals(productId, response.productId());
        assertEquals(locationId, response.locationId());
        verify(stockEntryRepository).save(any());
        verify(companyProductRepository).save(companyProduct);
        assertEquals(new BigDecimal("12.50"), companyProduct.getCostPrice());
    }

    @Test
    @DisplayName("Deve atualizar quantidade de entrada de estoque existente")
    void shouldUpdateQuantityOfExistingStockEntry() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Location location = buildLocation(locationId);
        StockRequest request = new StockRequest(productId, locationId, 80, new BigDecimal("9.90"));
        Stock existing = new Stock(UUID.randomUUID(), null, product, location, 30);
        Stock updated = new Stock(existing.getId(), null, product, location, 80);
        CompanyProduct companyProduct = new CompanyProduct();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(companyProductRepository.findByCompanyIdAndProductId(companyId, productId)).thenReturn(Optional.of(companyProduct));
        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductIdAndLocationId(productId, locationId)).thenReturn(Optional.of(existing));
        when(stockRepository.save(existing)).thenReturn(updated);

        StockResponse response = stockService.setStock(request);

        assertEquals(80, response.quantity());
        verify(stockEntryRepository).save(any());
        verify(stockRepository).save(existing);
    }

    @Test
    @DisplayName("Deve exigir custo quando houver entrada de mercadoria")
    void shouldRequireUnitCostWhenIncreasingStock() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Location location = buildLocation(locationId);
        Stock existing = new Stock(UUID.randomUUID(), null, product, location, 30);
        CompanyProduct companyProduct = new CompanyProduct();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(companyProductRepository.findByCompanyIdAndProductId(companyId, productId)).thenReturn(Optional.of(companyProduct));
        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductIdAndLocationId(productId, locationId)).thenReturn(Optional.of(existing));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> stockService.setStock(new StockRequest(productId, locationId, 40, null)));

        assertTrue(error.getMessage().contains("custo"));
        verify(stockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve exigir custo quando nao houver acrescimo de estoque")
    void shouldNotRequireUnitCostWhenNotIncreasingStock() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Location location = buildLocation(locationId);
        Stock existing = new Stock(UUID.randomUUID(), null, product, location, 30);
        CompanyProduct companyProduct = new CompanyProduct();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(companyProductRepository.findByCompanyIdAndProductId(companyId, productId)).thenReturn(Optional.of(companyProduct));
        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductIdAndLocationId(productId, locationId)).thenReturn(Optional.of(existing));
        when(stockRepository.save(existing)).thenReturn(existing);

        StockResponse response = stockService.setStock(new StockRequest(productId, locationId, 20, null));

        assertEquals(20, response.quantity());
        verify(stockEntryRepository, never()).save(any());
        verify(companyProductRepository, never()).save(companyProduct);
    }

    @Test
    @DisplayName("Deve lancar excecao quando produto de outro tenant tentar definir estoque")
    void shouldThrowNotFoundWhenProductDoesNotBelongToCurrentTenantOnSetStock() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.setStock(new StockRequest(productId, UUID.randomUUID(), 10, new BigDecimal("5.00"))));
        verifyNoInteractions(locationRepository, stockRepository, stockEntryRepository, companyProductRepository);
    }

    @Test
    @DisplayName("Deve lancar excecao quando produto nao estiver configurado na filial atual")
    void shouldThrowNotFoundWhenProductIsNotConfiguredForCompany() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(companyProductRepository.findByCompanyIdAndProductId(companyId, productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.setStock(new StockRequest(productId, locationId, 10, new BigDecimal("5.00"))));
        verifyNoInteractions(locationRepository, stockRepository, stockEntryRepository);
    }

    @Test
    @DisplayName("Deve lancar excecao quando localizacao nao encontrada ao definir estoque")
    void shouldThrowNotFoundWhenLocationNotFoundOnSetStock() {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Product product = buildProduct(productId);
        CompanyProduct companyProduct = new CompanyProduct();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(companyProductRepository.findByCompanyIdAndProductId(companyId, productId)).thenReturn(Optional.of(companyProduct));
        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.setStock(new StockRequest(productId, locationId, 10, new BigDecimal("5.00"))));
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("Deve retornar todas as entradas de estoque para um produto do tenant atual")
    void shouldReturnAllStockEntriesForProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId);
        Stock s1 = new Stock(UUID.randomUUID(), null, product, buildLocation(UUID.randomUUID()), 30);
        Stock s2 = new Stock(UUID.randomUUID(), null, product, buildLocation(UUID.randomUUID()), 20);

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(stockRepository.findAllByProductIdOrderByQuantityDesc(eq(productId), any())).thenReturn(List.of(s1, s2));

        List<StockResponse> result = stockService.getStockByProduct(productId);

        assertEquals(2, result.size());
        assertEquals(30, result.get(0).quantity());
        assertEquals(20, result.get(1).quantity());
    }

    @Test
    @DisplayName("Deve lancar excecao quando produto nao encontrado ao consultar estoque")
    void shouldThrowNotFoundWhenProductNotFoundOnGetStockByProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.getStockByProduct(productId));
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("Deve retornar o total de estoque para o produto")
    void shouldReturnTotalStockSumForProduct() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId);
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(stockRepository.sumQuantityByProductId(eq(productId), any())).thenReturn(150);

        int total = stockService.getTotalStockByProduct(productId);

        assertEquals(150, total);
    }

    @Test
    @DisplayName("Deve lancar excecao quando produto nao encontrado ao consultar total de estoque")
    void shouldThrowNotFoundWhenProductNotFoundOnGetTotalStock() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.getTotalStockByProduct(productId));
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("Deve retornar todas as entradas de estoque para uma localizacao")
    void shouldReturnAllStockEntriesForLocation() {
        UUID locationId = UUID.randomUUID();
        Location location = buildLocation(locationId);
        Product product = buildProduct(UUID.randomUUID());
        Stock stock = new Stock(UUID.randomUUID(), null, product, location, 10);

        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.of(location));
        when(stockRepository.findAllByLocationIdAndCompanyId(locationId, companyId)).thenReturn(List.of(stock));

        List<StockResponse> result = stockService.getStockByLocation(locationId);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).quantity());
    }

    @Test
    @DisplayName("Deve lancar excecao quando localizacao nao encontrada ao consultar estoque")
    void shouldThrowNotFoundWhenLocationNotFoundOnGetStockByLocation() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.findByIdAndCompanyId(locationId, companyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> stockService.getStockByLocation(locationId));
        verifyNoInteractions(stockRepository);
    }
}
