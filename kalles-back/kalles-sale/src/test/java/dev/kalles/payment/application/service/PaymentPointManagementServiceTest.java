package dev.kalles.payment.application.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.payment.application.port.in.command.ActivatePaymentTerminalCommand;
import dev.kalles.payment.application.port.in.command.CreatePaymentPointCommand;
import dev.kalles.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.payment.application.port.out.PaymentTerminalPort;
import dev.kalles.payment.application.port.out.PaymentTerminalRepository;
import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;
import dev.kalles.payment.exception.PaymentProviderIntegrationException;
import dev.kalles.payment.exception.PaymentStoreNotConfiguredException;
import dev.kalles.security.exception.TenantContextRequiredException;
import dev.kalles.shared.exception.NotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

        assertThrows(NotFoundException.class, () ->
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

        assertThrows(NotFoundException.class, () ->
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
    void shouldAnswerNotFoundWhenTheSerialIsNotAmongTheTerminalsOfThePoint() {
        UUID companyId = UUID.randomUUID();
        UUID cashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = mock(CashRegister.class);
        PaymentTerminalPort terminalPort = mock(PaymentTerminalPort.class);

        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyId, "A", TENANT_ID, null, null, null, null, null, null)));
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentStore(UUID.randomUUID(), companyId, PaymentProvider.MERCADO_PAGO, "tenant-store", "store-1")));
        when(cashRegister.getId()).thenReturn(cashRegisterId);
        when(cashRegisterRepository.findAllByCompanyIdAndActiveTrueOrderByCodeAsc(companyId))
                .thenReturn(List.of(cashRegister));
        when(paymentPointRepository.findByCashRegisterIdAndProvider(cashRegisterId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentPoint(UUID.randomUUID(), cashRegisterId, PaymentProvider.MERCADO_PAGO, "CAIXA-01", "point-1")));
        when(portFactory.terminal(PaymentProvider.MERCADO_PAGO)).thenReturn(terminalPort);
        when(terminalPort.listTerminals("store-1", "point-1"))
                .thenReturn(List.of(new PaymentTerminal("PAX_A910__OTHER", "point-1", "store-1", null, null)));

        assertThrows(NotFoundException.class, () ->
                service.execute(new ActivatePaymentTerminalCommand(
                        PaymentProvider.MERCADO_PAGO,
                        "store-1",
                        "point-1",
                        "SERIAL-1",
                        null
                )));

        verify(paymentTerminalRepository, never()).save(any());
    }

    @Test
    void shouldNotStoreTheTerminalWhenTheProviderFailsToChangeItsOperationMode() {
        UUID companyId = UUID.randomUUID();
        UUID cashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = mock(CashRegister.class);
        PaymentTerminalPort terminalPort = mock(PaymentTerminalPort.class);

        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyId, "A", TENANT_ID, null, null, null, null, null, null)));
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentStore(UUID.randomUUID(), companyId, PaymentProvider.MERCADO_PAGO, "tenant-store", "store-1")));
        when(cashRegister.getId()).thenReturn(cashRegisterId);
        when(cashRegisterRepository.findAllByCompanyIdAndActiveTrueOrderByCodeAsc(companyId))
                .thenReturn(List.of(cashRegister));
        when(paymentPointRepository.findByCashRegisterIdAndProvider(cashRegisterId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentPoint(UUID.randomUUID(), cashRegisterId, PaymentProvider.MERCADO_PAGO, "CAIXA-01", "point-1")));
        when(portFactory.terminal(PaymentProvider.MERCADO_PAGO)).thenReturn(terminalPort);
        when(terminalPort.listTerminals("store-1", "point-1"))
                .thenReturn(List.of(new PaymentTerminal("PAX_A910__SERIAL-1", "point-1", "store-1", null, TerminalOperationMode.STANDALONE)));
        doThrow(new PaymentProviderIntegrationException("falha no provedor"))
                .when(terminalPort).changeOperationMode("PAX_A910__SERIAL-1", TerminalOperationMode.POINT_OF_SALE);

        assertThrows(PaymentProviderIntegrationException.class, () ->
                service.execute(new ActivatePaymentTerminalCommand(
                        PaymentProvider.MERCADO_PAGO,
                        "store-1",
                        "point-1",
                        "SERIAL-1",
                        null
                )));

        verify(paymentTerminalRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToCreateThePointWhenTheCompanyHasNoProviderStore() {
        UUID companyId = UUID.randomUUID();
        UUID cashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = mock(CashRegister.class);

        when(paymentPointRepository.findByCashRegisterIdAndProvider(cashRegisterId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentPoint(UUID.randomUUID(), cashRegisterId, PaymentProvider.MERCADO_PAGO, "CAIXA-01", null)));
        when(companyRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(new Company(companyId, "A", TENANT_ID, null, null, null, null, null, null)));
        when(cashRegisterRepository.findByIdAndCompanyId(cashRegisterId, companyId)).thenReturn(Optional.of(cashRegister));
        when(cashRegister.getCompanyId()).thenReturn(companyId);
        when(paymentStoreRepository.findByCompanyIdAndProvider(companyId, PaymentProvider.MERCADO_PAGO))
                .thenReturn(Optional.of(new PaymentStore(UUID.randomUUID(), companyId, PaymentProvider.MERCADO_PAGO, "FILIAL-01", null)));

        assertThrows(PaymentStoreNotConfiguredException.class, () ->
                service.execute(new CreatePaymentPointCommand(
                        PaymentProvider.MERCADO_PAGO,
                        cashRegisterId,
                        "CAIXA-01",
                        null
                )));

        verify(portFactory, never()).point(PaymentProvider.MERCADO_PAGO);
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
