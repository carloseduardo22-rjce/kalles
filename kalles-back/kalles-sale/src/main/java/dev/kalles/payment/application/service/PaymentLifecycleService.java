package dev.kalles.payment.application.service;

import dev.kalles.core.service.CheckoutSessionService;
import dev.kalles.core.service.Session;
import dev.kalles.payment.application.port.in.ProcessPaymentUseCase;
import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentLifecycleService implements ProcessPaymentUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentOrderRepository paymentOrderRepository;
    private final CheckoutSessionService checkoutSessionService;

    public PaymentLifecycleService(
            PaymentProviderPortFactory portFactory,
            PaymentOrderRepository paymentOrderRepository,
            CheckoutSessionService checkoutSessionService
    ) {
        this.portFactory = portFactory;
        this.paymentOrderRepository = paymentOrderRepository;
        this.checkoutSessionService = checkoutSessionService;
    }

    @Override
    public PaymentResult execute(PaymentCommand command) {
        PaymentCommand normalizedCommand = ensureIdempotency(command);
        validateSessionAllowsElectronicPayments(normalizedCommand);
        PaymentResult result = portFactory.gateway(normalizedCommand.provider()).processPayment(normalizedCommand);

        if (normalizedCommand.flow() == PaymentFlow.TERMINAL) {
            paymentOrderRepository.save(new PaymentOrder(
                    normalizedCommand.provider(),
                    result.providerOrderId(),
                    result.providerPaymentId(),
                    result.status(),
                    normalizedCommand.externalReference(),
                    normalizedCommand.amount(),
                    normalizedCommand.idempotencyKey(),
                    normalizedCommand.flow(),
                    normalizedCommand.methodType()
            ));
        }

        return result;
    }

    private PaymentCommand ensureIdempotency(PaymentCommand command) {
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            return command;
        }

        return new PaymentCommand(
                command.provider(),
                command.flow(),
                command.externalReference(),
                command.amount(),
                command.targetId(),
                UUID.randomUUID().toString(),
                command.description(),
                command.methodType(),
                command.metadata()
        );
    }

    private void validateSessionAllowsElectronicPayments(PaymentCommand command) {
        checkoutSessionService.findByToken(command.externalReference())
            .ifPresent(this::ensureElectronicPaymentsAllowed);
    }

    private void ensureElectronicPaymentsAllowed(Session session) {
        if (!session.allowsElectronicPayments()) {
            throw new IllegalStateException(
                "Esta sessao foi aberta em modo somente dinheiro. PIX, vouchers e cartoes estao indisponiveis."
            );
        }
    }
}
