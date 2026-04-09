package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.payment.application.port.in.ProcessPaymentUseCase;
import dev.kalles.sale.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.sale.payment.domain.PaymentCommand;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentLifecycleService implements ProcessPaymentUseCase {

    private final PaymentProviderPortFactory portFactory;
    private final PaymentOrderRepository paymentOrderRepository;

    public PaymentLifecycleService(
            PaymentProviderPortFactory portFactory,
            PaymentOrderRepository paymentOrderRepository
    ) {
        this.portFactory = portFactory;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Override
    public PaymentResult execute(PaymentCommand command) {
        PaymentCommand normalizedCommand = ensureIdempotency(command);
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
}
