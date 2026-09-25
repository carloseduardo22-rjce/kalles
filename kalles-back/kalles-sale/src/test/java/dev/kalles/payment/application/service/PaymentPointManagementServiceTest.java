package dev.kalles.payment.application.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.payment.application.port.in.command.ActivatePaymentTerminalCommand;
import dev.kalles.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.payment.application.port.out.PaymentTerminalRepository;
import dev.kalles.payment.domain.PaymentPoint;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentPointManagementServiceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();

    @RegisterExtension
    static final RequestContextExtension REQUEST_CONTEXT = RequestContextExtension.tenant(TENANT_ID);

    @Mock
    private PaymentProviderPortFactory portFactory;

    @Mock
    private PaymentPointRepository paymentPointRepository;

    @Mock
    private PaymentStoreRepository paymentStoreRepository;

    @Mock
    private PaymentTerminalRepository paymentTerminalRepository;

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PaymentPointManagementService service;

    @Test
    void shouldRejectTerminalListingWhenStoreDoesNotBelongToCurrentTenant() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyId, "A", TENANT_ID, null, null, null, null, null, null)));
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(new ListPaymentTerminalsQuery(
                        PaymentProvider.MERCADO_PAGO,
                        "foreign-store",
                        "foreign-point"
                )));

        verify(portFactory, never()).terminal(PaymentProvider.MERCADO_PAGO);
    }

    @Test
    void shouldRejectTerminalActivationWhenPointDoesNotBelongToCurrentTenant() {
        UUID companyId = UUID.randomUUID();
        UUID cashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = mock(CashRegister.class);

        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyId, "A", TENANT_ID, null, null, null, null, null, null)));
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentStore(UUID.randomUUID(), companyId, PaymentProvider.MERCADO_PAGO, "tenant-store", "store-1")));
        when(cashRegister.getId()).thenReturn(cashRegisterId);
        when(cashRegisterRepository.findAllByCompanyIdAndActiveTrueOrderByCodeAsc(companyId))
                .thenReturn(List.of(cashRegister));
        when(paymentPointRepository.findByCashRegisterIdAndProvider(cashRegisterId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(new ActivatePaymentTerminalCommand(
                        PaymentProvider.MERCADO_PAGO,
                        "store-1",
                        "foreign-point",
                        "SERIAL-1",
                        null
                )));

        verify(portFactory, never()).terminal(PaymentProvider.MERCADO_PAGO);
    }

    @Test
    void shouldRequireTenantContextForTerminalListing() {
        RequestContext.runWithin(RequestContext.empty(), () ->
                assertThrows(TenantContextRequiredException.class, () ->
                        service.execute(new ListPaymentTerminalsQuery(
                                PaymentProvider.MERCADO_PAGO,
                                "store-1",
                                "point-1"
                        ))));
    }
}
