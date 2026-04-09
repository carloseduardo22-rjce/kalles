package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.cashregister.entity.CashRegister;
import dev.kalles.sale.cashregister.repository.CashRegisterRepository;
import dev.kalles.sale.payment.application.port.in.ActivatePaymentTerminalUseCase;
import dev.kalles.sale.payment.application.port.in.CreatePaymentPointUseCase;
import dev.kalles.sale.payment.application.port.in.ListPaymentPointsUseCase;
import dev.kalles.sale.payment.application.port.in.ListPaymentTerminalsUseCase;
import dev.kalles.sale.payment.application.port.in.command.ActivatePaymentTerminalCommand;
import dev.kalles.sale.payment.application.port.in.command.CreatePaymentPointCommand;
import dev.kalles.sale.payment.application.port.in.command.ListPaymentTerminalsQuery;
import dev.kalles.sale.payment.application.port.out.PaymentPointRepository;
import dev.kalles.sale.payment.application.port.out.PaymentStoreRepository;
import dev.kalles.sale.payment.application.port.out.PaymentTerminalRepository;
import dev.kalles.sale.payment.domain.PaymentPoint;
import dev.kalles.sale.payment.domain.PaymentPointDescriptor;
import dev.kalles.sale.payment.domain.PaymentPointView;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;
import dev.kalles.sale.payment.domain.PaymentTerminal;
import dev.kalles.sale.payment.domain.TerminalOperationMode;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public PaymentPointManagementService(
            PaymentProviderPortFactory portFactory,
            PaymentPointRepository paymentPointRepository,
            PaymentStoreRepository paymentStoreRepository,
            PaymentTerminalRepository paymentTerminalRepository,
            CashRegisterRepository cashRegisterRepository
    ) {
        this.portFactory = portFactory;
        this.paymentPointRepository = paymentPointRepository;
        this.paymentStoreRepository = paymentStoreRepository;
        this.paymentTerminalRepository = paymentTerminalRepository;
        this.cashRegisterRepository = cashRegisterRepository;
    }

    @Override
    public PaymentPoint execute(CreatePaymentPointCommand command) {
        PaymentPoint point = paymentPointRepository.findByExternalReferenceAndProvider(command.externalReference(), command.provider())
                .orElseGet(() -> createDraftPoint(command));

        if (point.hasProviderPoint()) {
            return point;
        }

        CashRegister cashRegister = cashRegisterRepository.findById(command.cashRegisterId())
                .orElseThrow(() -> new IllegalArgumentException("Cash register not found: " + command.cashRegisterId()));

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
        List<PaymentTerminal> terminals = portFactory.terminal(query.provider()).listTerminals(query.storeId(), query.pointId());
        paymentTerminalRepository.saveAll(terminals);
        return terminals;
    }

    @Override
    public void execute(ActivatePaymentTerminalCommand command) {
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

        return paymentPointRepository.findByExternalReferenceAndProvider(command.externalReference(), command.provider())
                .orElseThrow(() -> new IllegalStateException("Point draft could not be reloaded after save"));
    }
}
