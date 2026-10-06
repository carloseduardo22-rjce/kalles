package dev.kalles.product.service;

import dev.kalles.inventory.repository.ProductStockQueryRepository;
import dev.kalles.product.dto.CompanyProductListItem;
import dev.kalles.product.dto.ProductCatalogResponse;
import dev.kalles.product.dto.ProductRequest;
import dev.kalles.product.entity.CompanyProduct;
import dev.kalles.product.entity.Product;
import dev.kalles.product.exception.ProductBarcodeAlreadyExistsException;
import dev.kalles.product.exception.ProductInternalCodeAlreadyExistsException;
import dev.kalles.product.repository.CompanyProductReadRepository;
import dev.kalles.product.repository.CompanyProductRepository;
import dev.kalles.product.repository.ProductRepository;
import dev.kalles.product.service.ProductService;
import dev.kalles.security.exception.TenantContextRequiredException;
import dev.kalles.shared.exception.NotFoundException;
import dev.kalles.testsupport.RequestContextExtension;
import dev.kalles.security.context.RequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.tenantAndCompany(TENANT_ID, COMPANY_ID);

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
        when(companyProductReadRepository.findCatalogItem(COMPANY_ID, productId)).thenReturn(Optional.of(
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

        ProductInternalCodeAlreadyExistsException error = assertThrows(ProductInternalCodeAlreadyExistsException.class,
                () -> productService.create(request));

        assertEquals("PRODUCT_INTERNAL_CODE_ALREADY_EXISTS", error.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar codigo de barras duplicado no mesmo tenant")
    void shouldRejectDuplicateBarcodeInsideSameTenant() {
        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );

        when(productRepository.findByInternalCodeAndTenantId("ARZ-001", TENANT_ID)).thenReturn(Optional.empty());
        when(productRepository.findByBarcodeAndTenantId("789100000001", TENANT_ID))
                .thenReturn(Optional.of(new Product()));

        ProductBarcodeAlreadyExistsException error = assertThrows(ProductBarcodeAlreadyExistsException.class,
                () -> productService.create(request));

        assertEquals("PRODUCT_BARCODE_ALREADY_EXISTS", error.getCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar atualizacao com codigo interno de outro produto")
    void shouldRejectUpdateWithInternalCodeOfAnotherProduct() {
        UUID productId = UUID.randomUUID();
        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );
        Product current = new Product();
        current.setId(productId);
        Product another = new Product();
        another.setId(UUID.randomUUID());

        when(productRepository.findByIdAndTenantId(productId, TENANT_ID)).thenReturn(Optional.of(current));
        when(productRepository.findByInternalCodeAndTenantId("ARZ-001", TENANT_ID)).thenReturn(Optional.of(another));

        assertThrows(ProductInternalCodeAlreadyExistsException.class, () -> productService.update(productId, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir tenant no contexto para criar produto")
    void shouldRequireTenantContextWhenCreatingProduct() {
        ProductRequest request = new ProductRequest(
                "Arroz Tipo 1",
                "ARZ-001",
                "789100000001",
                "Pacote 5kg",
                new BigDecimal("32.90"),
                new BigDecimal("24.50")
        );

        RequestContext.runWithin(RequestContext.empty().withCompany(COMPANY_ID), () ->
                assertThrows(TenantContextRequiredException.class, () -> productService.create(request)));
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
