package dev.kalles.payment.application.service;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.company.entity.Company;
import dev.kalles.company.repository.CompanyRepository;
import dev.kalles.payment.application.port.in.ActivatePaymentTerminalUseCase;
import dev.kalles.payment.application.port.in.CreatePaymentPointUseCase;
import dev.kalles.payment.application.port.in.ListPaymentPointsUseCase;
import dev.kalles.payment.application.port.in.ListPaymentTerminalsUseCase;
import dev.kalles.payment.application.port.in.command.ActivatePaymentTerminalCommand;
import dev.kalles.payment.application.port.in.command.CreatePaymentPointCommand;
import dev.kalles.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.payment.application.port.out.PaymentTerminalRepository;
import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentPointDescriptor;
import dev.kalles.payment.domain.PaymentPointView;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.payment.domain.PaymentTerminal;
import dev.kalles.payment.domain.TerminalOperationMode;
import dev.kalles.payment.exception.PaymentTenantContextException;
import dev.kalles.security.context.TenantContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentPointManagementService implements
        CreatePaymentPointUseCase,
        ListPaymentPointsUseCase,
        ListPaymentTerminalsUseCase,
        ActivatePaymentTerminalUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentPointRepository paymentPointRepository;
    private final PaymentStoreRepository paymentStoreRepository;
    private final PaymentTerminalRepository paymentTerminalRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final CompanyRepository companyRepository;

    public PaymentPointManagementService(
            PaymentProviderPortFactory portFactory,
            PaymentPointRepository paymentPointRepository,
            PaymentStoreRepository paymentStoreRepository,
            PaymentTerminalRepository paymentTerminalRepository,
            CashRegisterRepository cashRegisterRepository,
            CompanyRepository companyRepository
    ) {
        this.portFactory = portFactory;
        this.paymentPointRepository = paymentPointRepository;
        this.paymentStoreRepository = paymentStoreRepository;
        this.paymentTerminalRepository = paymentTerminalRepository;
        this.cashRegisterRepository = cashRegisterRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public PaymentPoint execute(CreatePaymentPointCommand command) {
        PaymentPoint point = paymentPointRepository.findByCashRegisterIdAndProvider(command.cashRegisterId(), command.provider())
                .orElseGet(() -> createDraftPoint(command));

        if (point.hasProviderPoint()) {
            return point;
        }

        CashRegister cashRegister = findAccessibleCashRegister(command.cashRegisterId());

        PaymentStore store = paymentStoreRepository.findByCompanyIdAndProvider(cashRegister.getCompanyId(), command.provider())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment store mapping not found for company " + cashRegister.getCompanyId() + " and provider " + command.provider()
                ));

        if (!store.hasProviderStore()) {
            throw new IllegalStateException("Company does not have a payment store configured for provider " + command.provider());
        }

        PaymentPoint createdPoint = portFactory.point(command.provider()).createPoint(
                point,
                store,
                new PaymentPointDescriptor(cashRegister.getDescription(), cashRegister.getCode())
        );

        if (createdPoint.providerPointId() != null && !createdPoint.providerPointId().isBlank()) {
            paymentPointRepository.updateProviderPointId(point.id(), createdPoint.providerPointId());
            return point.withProviderPointId(createdPoint.providerPointId());
        }

        return createdPoint;
    }

    @Override
    public List<PaymentPointView> execute(PaymentProvider provider) {
        return portFactory.point(provider).listPoints();
    }

    @Override
    public List<PaymentTerminal> execute(ListPaymentTerminalsQuery query) {
        ensureAccessibleTerminalScope(query.provider(), query.storeId(), query.pointId());
        List<PaymentTerminal> terminals = portFactory.terminal(query.provider()).listTerminals(query.storeId(), query.pointId());
        paymentTerminalRepository.saveAll(terminals);
        return terminals;
    }

    @Override
    public void execute(ActivatePaymentTerminalCommand command) {
        ensureAccessibleTerminalScope(command.provider(), command.storeId(), command.pointId());
        List<PaymentTerminal> terminals = portFactory.terminal(command.provider()).listTerminals(command.storeId(), command.pointId());
        if (terminals.isEmpty()) {
            throw new IllegalStateException("No terminal associated with the informed store and point");
        }

        PaymentTerminal targetTerminal = terminals.stream()
                .filter(terminal -> terminal.id() != null && terminal.id().endsWith(command.terminalSerial()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Terminal not found for informed serial"));

        if (targetTerminal.operationMode() != TerminalOperationMode.POINT_OF_SALE) {
            boolean success = portFactory.terminal(command.provider())
                    .changeOperationMode(targetTerminal.id(), TerminalOperationMode.POINT_OF_SALE);
            if (!success) {
                throw new IllegalStateException("Failed to change terminal operation mode");
            }
        }

        paymentTerminalRepository.save(targetTerminal.withOperationMode(TerminalOperationMode.POINT_OF_SALE));
    }

    private PaymentPoint createDraftPoint(CreatePaymentPointCommand command) {
        paymentPointRepository.save(new PaymentPoint(
                null,
                command.cashRegisterId(),
                command.provider(),
                command.externalReference(),
                null
        ));

        return paymentPointRepository.findByCashRegisterIdAndProvider(command.cashRegisterId(), command.provider())
                .orElseThrow(() -> new IllegalStateException("Point draft could not be reloaded after save"));
    }

    private CashRegister findAccessibleCashRegister(UUID cashRegisterId) {
        return accessibleCompanies().stream()
                .map(company -> cashRegisterRepository.findByIdAndCompanyId(cashRegisterId, company.getId()))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cash register not found: " + cashRegisterId));
    }

    private void ensureAccessibleTerminalScope(PaymentProvider provider, String storeId, String pointId) {
        boolean accessibleStore = accessibleCompanies().stream()
                .map(company -> paymentStoreRepository.findByCompanyIdAndProvider(company.getId(), provider))
                .flatMap(Optional::stream)
                .anyMatch(store -> storeId.equals(store.providerStoreId()));

        if (!accessibleStore) {
            throw new IllegalArgumentException("Payment store not found for current tenant");
        }

        boolean accessiblePoint = accessibleCompanies().stream()
                .flatMap(company -> cashRegisterRepository.findAllByCompanyIdAndActiveTrueOrderByCodeAsc(company.getId()).stream())
                .map(cashRegister -> paymentPointRepository.findByCashRegisterIdAndProvider(cashRegister.getId(), provider))
                .flatMap(Optional::stream)
                .anyMatch(point -> pointId.equals(point.providerPointId()));

        if (!accessiblePoint) {
            throw new IllegalArgumentException("Payment point not found for current tenant");
        }
    }

    private List<Company> accessibleCompanies() {
        return companyRepository.findByTenantId(getCurrentTenantId());
    }

    private UUID getCurrentTenantId() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new PaymentTenantContextException("Tenant context is required for this operation");
        }
        return tenantId;
    }
}
