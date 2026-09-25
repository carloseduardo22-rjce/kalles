package dev.kalles.payment.application.service;

import dev.kalles.company.entity.Company;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.payment.application.port.in.command.CreatePaymentStoreCommand;
import dev.kalles.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.security.exception.TenantContextRequiredException;
import dev.kalles.testsupport.RequestContextExtension;
import dev.kalles.security.context.RequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.tenant(TENANT_ID);

    @Mock
    private PaymentProviderPortFactory portFactory;

    @Mock
    private PaymentStoreRepository paymentStoreRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PaymentStoreManagementService service;

    @Test
    void shouldRejectStoreCreationForCompanyFromAnotherTenant() {
        UUID companyId = UUID.randomUUID();

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

        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyA, "A", TENANT_ID, null, null, null, null, null, null)));
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyA, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentStore(UUID.randomUUID(), companyA, PaymentProvider.MERCADO_PAGO, "tenant-a-store", "store-1")));

        Optional<PaymentStore> found = service.findByExternalReference(PaymentProvider.MERCADO_PAGO, "tenant-b-store");

        assertEquals(Optional.empty(), found);
    }

    @Test
    void shouldRequireTenantContextForStatusLookup() {
        RequestContext.runWithin(RequestContext.empty(), () ->
                assertThrows(TenantContextRequiredException.class, () ->
                        service.findByExternalReference(PaymentProvider.MERCADO_PAGO, "external-ref")));
    }
}
