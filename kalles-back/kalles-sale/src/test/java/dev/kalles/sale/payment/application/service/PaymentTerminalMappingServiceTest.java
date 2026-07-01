package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.core.entity.Company;
import dev.kalles.sale.core.repository.CompanyRepository;
import dev.kalles.sale.payment.application.port.in.command.GetPaymentTerminalMappingQuery;
import dev.kalles.sale.payment.application.port.in.command.MapPaymentTerminalCommand;
import dev.kalles.sale.payment.application.port.out.PaymentTerminalMappingRepository;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentTerminalMapping;
import dev.kalles.sale.security.context.CompanyContextHolder;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class PaymentTerminalMappingServiceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID CASH_REGISTER_ID = UUID.randomUUID();

    private PaymentTerminalMappingRepository mappingRepository;
    private CashRegisterRepository cashRegisterRepository;
    private CompanyRepository companyRepository;
    private PaymentTerminalMappingService service;

    @BeforeEach
    void setUp() {
        mappingRepository = mock(PaymentTerminalMappingRepository.class);
        cashRegisterRepository = mock(CashRegisterRepository.class);
        companyRepository = mock(CompanyRepository.class);
        service = new PaymentTerminalMappingService(mappingRepository, cashRegisterRepository, companyRepository);

        TenantContextHolder.setTenantId(TENANT_ID);
        CompanyContextHolder.setCompanyId(COMPANY_ID);
        when(companyRepository.findByIdAndTenantId(COMPANY_ID, TENANT_ID))
                .thenReturn(Optional.of(new Company(COMPANY_ID, "Matriz", TENANT_ID, null, null, null, null, null, null)));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        CompanyContextHolder.clear();
    }

    @Test
    void shouldMapStoneTerminalToAccessibleCashRegister() {
        CashRegister cashRegister = accessibleCashRegister();
        when(cashRegisterRepository.findByIdAndCompanyId(CASH_REGISTER_ID, COMPANY_ID))
                .thenReturn(Optional.of(cashRegister));
        when(mappingRepository.findActiveByCompanyIdAndProviderAndTerminalSerial(
                COMPANY_ID,
                PaymentProvider.STONE,
                "6N021234"
        )).thenReturn(Optional.empty());
        when(mappingRepository.findActiveByCashRegisterIdAndProvider(CASH_REGISTER_ID, PaymentProvider.STONE))
                .thenReturn(Optional.empty());
        when(mappingRepository.save(any(PaymentTerminalMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentTerminalMapping result = service.execute(new MapPaymentTerminalCommand(
                CASH_REGISTER_ID,
                PaymentProvider.STONE,
                " 6n021234 "
        ));

        assertThat(result.tenantId()).isEqualTo(TENANT_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.cashRegisterId()).isEqualTo(CASH_REGISTER_ID);
        assertThat(result.terminalSerial()).isEqualTo("6N021234");
        verify(mappingRepository).save(any(PaymentTerminalMapping.class));
    }

    @Test
    void shouldRejectSerialAlreadyMappedToAnotherCashRegisterInSameCompany() {
        UUID otherCashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = accessibleCashRegister();
        when(cashRegisterRepository.findByIdAndCompanyId(CASH_REGISTER_ID, COMPANY_ID))
                .thenReturn(Optional.of(cashRegister));
        when(mappingRepository.findActiveByCompanyIdAndProviderAndTerminalSerial(
                COMPANY_ID,
                PaymentProvider.STONE,
                "6N021234"
        )).thenReturn(Optional.of(new PaymentTerminalMapping(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                otherCashRegisterId,
                PaymentProvider.STONE,
                "6N021234",
                true,
                null,
                null
        )));

        assertThatThrownBy(() -> service.execute(new MapPaymentTerminalCommand(
                CASH_REGISTER_ID,
                PaymentProvider.STONE,
                "6N021234"
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Este numero de serie ja esta vinculado a outro caixa desta filial.");
    }

    @Test
    void shouldRejectCashRegisterFromAnotherCompany() {
        when(cashRegisterRepository.findByIdAndCompanyId(CASH_REGISTER_ID, COMPANY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetPaymentTerminalMappingQuery(
                CASH_REGISTER_ID,
                PaymentProvider.STONE
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Caixa nao encontrado na filial ativa.");
    }

    @Test
    void shouldRejectCompanyOutsideCurrentTenantBeforeReadingCashRegister() {
        when(companyRepository.findByIdAndTenantId(COMPANY_ID, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new MapPaymentTerminalCommand(
                CASH_REGISTER_ID,
                PaymentProvider.STONE,
                "6N021234"
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filial nao encontrada para o tenant atual.");

        verify(cashRegisterRepository, never()).findByIdAndCompanyId(CASH_REGISTER_ID, COMPANY_ID);
    }

    @Test
    void shouldListOnlyMappingsFromCurrentCompanyAndProvider() {
        PaymentTerminalMapping mapping = new PaymentTerminalMapping(
                UUID.randomUUID(),
                TENANT_ID,
                COMPANY_ID,
                CASH_REGISTER_ID,
                PaymentProvider.STONE,
                "6N021234",
                true,
                null,
                null
        );
        when(mappingRepository.findActiveByCompanyIdAndProvider(COMPANY_ID, PaymentProvider.STONE))
                .thenReturn(List.of(mapping));

        List<PaymentTerminalMapping> result = service.execute(PaymentProvider.STONE);

        assertThat(result).containsExactly(mapping);
        verify(mappingRepository).findActiveByCompanyIdAndProvider(COMPANY_ID, PaymentProvider.STONE);
    }

    private CashRegister accessibleCashRegister() {
        CashRegister cashRegister = mock(CashRegister.class);
        when(cashRegister.getId()).thenReturn(CASH_REGISTER_ID);
        when(cashRegister.isActive()).thenReturn(true);
        return cashRegister;
    }
}
