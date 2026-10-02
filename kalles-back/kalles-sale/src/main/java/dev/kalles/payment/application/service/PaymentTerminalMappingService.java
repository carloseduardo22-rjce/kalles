package dev.kalles.payment.application.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.payment.application.port.in.GetPaymentTerminalMappingUseCase;
import dev.kalles.payment.application.port.in.ListPaymentTerminalMappingsUseCase;
import dev.kalles.payment.application.port.in.MapPaymentTerminalUseCase;
import dev.kalles.payment.application.port.in.command.GetPaymentTerminalMappingQuery;
import dev.kalles.payment.application.port.in.command.MapPaymentTerminalCommand;
import dev.kalles.payment.application.port.out.PaymentTerminalMappingRepository;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentTerminalMapping;
import dev.kalles.payment.exception.TerminalSerialAlreadyMappedException;
import dev.kalles.security.context.CompanyContextHolder;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.shared.exception.ForbiddenOperationException;
import dev.kalles.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentTerminalMappingService implements
        MapPaymentTerminalUseCase,
        GetPaymentTerminalMappingUseCase,
        ListPaymentTerminalMappingsUseCase {

    private final PaymentTerminalMappingRepository mappingRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CompanyRepository companyRepository;

    public PaymentTerminalMappingService(
            PaymentTerminalMappingRepository mappingRepository,
            CashRegisterRepository cashRegisterRepository,
            CompanyRepository companyRepository
    ) {
        this.mappingRepository = mappingRepository;
        this.cashRegisterRepository = cashRegisterRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public PaymentTerminalMapping execute(MapPaymentTerminalCommand command) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        UUID companyId = CompanyContextHolder.requireCompanyId();
        ensureAccessibleCompany(companyId, tenantId);
        CashRegister cashRegister = findAccessibleCashRegister(command.cashRegisterId(), companyId);

        String normalizedSerial = PaymentTerminalMapping.normalizeSerial(command.terminalSerial());
        mappingRepository.findActiveByCompanyIdAndProviderAndTerminalSerial(
                companyId,
                command.provider(),
                normalizedSerial
        ).ifPresent(existing -> {
            if (!existing.cashRegisterId().equals(cashRegister.getId())) {
                throw new TerminalSerialAlreadyMappedException();
            }
        });

        PaymentTerminalMapping mapping = mappingRepository
                .findActiveByCashRegisterIdAndProvider(cashRegister.getId(), command.provider())
                .map(existing -> existing.withTerminalSerial(normalizedSerial))
                .orElseGet(() -> new PaymentTerminalMapping(
                        null,
                        tenantId,
                        companyId,
                        cashRegister.getId(),
                        command.provider(),
                        normalizedSerial,
                        true,
                        null,
                        null
                ));

        return mappingRepository.save(mapping);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTerminalMapping execute(GetPaymentTerminalMappingQuery query) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        UUID companyId = CompanyContextHolder.requireCompanyId();
        ensureAccessibleCompany(companyId, tenantId);
        CashRegister cashRegister = findAccessibleCashRegister(query.cashRegisterId(), companyId);

        return mappingRepository.findActiveByCashRegisterIdAndProvider(cashRegister.getId(), query.provider())
                .orElseThrow(() -> new NotFoundException("Nenhuma maquininha esta vinculada a este caixa."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTerminalMapping> execute(PaymentProvider provider) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        UUID companyId = CompanyContextHolder.requireCompanyId();
        ensureAccessibleCompany(companyId, tenantId);
        return mappingRepository.findActiveByCompanyIdAndProvider(companyId, provider);
    }

    private CashRegister findAccessibleCashRegister(UUID cashRegisterId, UUID companyId) {
        return cashRegisterRepository.findByIdAndCompanyId(cashRegisterId, companyId)
                .filter(CashRegister::isActive)
                .orElseThrow(() -> new NotFoundException("Caixa nao encontrado na filial ativa."));
    }

    private void ensureAccessibleCompany(UUID companyId, UUID tenantId) {
        if (companyRepository.findByIdAndTenantId(companyId, tenantId).isEmpty()) {
            throw new ForbiddenOperationException("Filial nao encontrada para o tenant atual.");
        }
    }
}
