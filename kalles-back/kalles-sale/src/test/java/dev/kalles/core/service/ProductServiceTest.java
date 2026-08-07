package dev.kalles.core.service;

import dev.kalles.core.dto.CompanyProductListItem;
import dev.kalles.core.dto.ProductCatalogResponse;
import dev.kalles.core.dto.ProductRequest;
import dev.kalles.core.entity.CompanyProduct;
import dev.kalles.core.entity.Product;
import dev.kalles.core.exception.NotFoundException;
import dev.kalles.core.repository.CompanyProductReadRepository;
import dev.kalles.core.repository.CompanyProductRepository;
import dev.kalles.core.repository.ProductRepository;
import dev.kalles.core.repository.ProductStockQueryRepository;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Servico de Produtos")
class ProductServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174501");
    private static final UUID COMPANY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174502");

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyProductRepository companyProductRepository;

    @Mock
    private CompanyProductReadRepository companyProductReadRepository;

    @Mock
    private ProductStockQueryRepository productStockQueryRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(TENANT_ID);
        CompanyContextHolder.setCompanyId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        CompanyContextHolder.clear();
    }

    @Test
    @DisplayName("Deve criar produto dentro do tenant atual")
    void shouldCreateProductInsideCurrentTenant() {
        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );
        UUID productId = UUID.randomUUID();
        Product savedProduct = new Product();
        savedProduct.setId(productId);
        savedProduct.setTenantId(TENANT_ID);
        savedProduct.setName(request.name());
        savedProduct.setInternalCode(request.internalCode());
        savedProduct.setBarcode(request.barcode());
        savedProduct.setDescription(request.description());

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productRepository.findByIdAndTenantId(productId, TENANT_ID)).thenReturn(Optional.of(savedProduct));
        when(companyProductReadRepository.listCatalog(COMPANY_ID, true)).thenReturn(List.of(
                new CompanyProductListItem(
                        productId,
                        request.name(),
                        request.internalCode(),
                        request.barcode(),
                        request.price(),
                        request.costPrice(),
                        request.description(),
                        true
                )
        ));

        ProductCatalogResponse response = productService.create(request);

        assertEquals(productId, response.id());
        assertEquals("Arroz Tipo 1", response.name());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(TENANT_ID, productCaptor.getValue().getTenantId());

        ArgumentCaptor<CompanyProduct> companyProductCaptor = ArgumentCaptor.forClass(CompanyProduct.class);
        verify(companyProductRepository).save(companyProductCaptor.capture());
        assertEquals(COMPANY_ID, companyProductCaptor.getValue().getCompanyId());
        assertTrue(companyProductCaptor.getValue().isActive());
    }

    @Test
    @DisplayName("Deve rejeitar codigo interno duplicado no mesmo tenant")
    void shouldRejectDuplicateInternalCodeInsideSameTenant() {
        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );

        when(productRepository.findByInternalCodeAndTenantId("ARZ-001", TENANT_ID))
                .thenReturn(Optional.of(new Product()));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> productService.create(request));

        assertTrue(error.getMessage().contains("codigo interno"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir tenant no contexto para criar produto")
    void shouldRequireTenantContextWhenCreatingProduct() {
        TenantContextHolder.clear();

        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> productService.create(request));

        assertTrue(error.getMessage().contains("tenant"));
    }

    @Test
    @DisplayName("Deve falhar ao buscar produto de outro tenant")
    void shouldThrowNotFoundWhenFindingProductFromAnotherTenant() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndTenantId(productId, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.findById(productId));
    }

    @Test
    @DisplayName("Deve falhar ao atualizar produto de outro tenant")
    void shouldThrowNotFoundWhenUpdatingProductFromAnotherTenant() {
        UUID productId = UUID.randomUUID();
        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );
        when(productRepository.findByIdAndTenantId(productId, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.update(productId, request));
    }
}
