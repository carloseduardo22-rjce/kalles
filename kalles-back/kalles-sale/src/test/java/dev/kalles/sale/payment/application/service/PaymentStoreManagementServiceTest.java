package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.repository.CompanyRepository;
import dev.kalles.sale.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.sale.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;
import dev.kalles.sale.payment.exception.PaymentTenantContextException;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentStoreManagementServiceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private PaymentProviderPortFactory portFactory;

    @Mock
    private PaymentStoreRepository paymentStoreRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PaymentStoreManagementService service;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldRejectStoreCreationForCompanyFromAnotherTenant() {
        UUID companyId = UUID.randomUUID();
        TenantContextHolder.setTenantId(TENANT_ID);

        when(companyRepository.findByIdAndTenantId(companyId, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(new CreatePaymentStoreCommand(
                        PaymentProvider.MERCADO_PAGO,
                        companyId,
                        "external-ref",
                        null
                )));

        verify(paymentStoreRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRestrictStatusLookupToCurrentTenantStores() {
        UUID companyA = UUID.randomUUID();
        TenantContextHolder.setTenantId(TENANT_ID);

        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyA, "A", TENANT_ID, null, null, null, null, null, null)));
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyA, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentStore(UUID.randomUUID(), companyA, PaymentProvider.MERCADO_PAGO, "tenant-a-store", "store-1")));

        Optional<PaymentStore> found = service.findByExternalReference(PaymentProvider.MERCADO_PAGO, "tenant-b-store");

        assertEquals(Optional.empty(), found);
    }

    @Test
    void shouldRequireTenantContextForStatusLookup() {
        assertThrows(PaymentTenantContextException.class, () ->
                service.findByExternalReference(PaymentProvider.MERCADO_PAGO, "external-ref"));
    }
}
