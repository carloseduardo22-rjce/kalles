package dev.kalles.sale.strategy;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.kalles.sale.enums.PaymentMethod;

@Component
public class DebitCardPaymentStrategy implements PaymentStrategy {

    private final boolean simulationEnabled;

    public DebitCardPaymentStrategy(@Value("${payments.simulation.enabled:false}") boolean simulationEnabled) {
        this.simulationEnabled = simulationEnabled;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.DEBIT_CARD;
    }

    @Override
    public PaymentResult process(BigDecimal amount) {
        if (!simulationEnabled) {
            // Sem integração real neste fluxo: confirmar aqui marcaria a venda como
            // paga sem nenhuma cobrança. Cartão deve ser cobrado via terminal
            // integrado (Mercado Pago), confirmado por webhook.
            throw new IllegalStateException(
                    "Pagamento com cartão de débito indisponível neste fluxo. Utilize o terminal integrado.");
        }
        String simulatedTransactionId = UUID.randomUUID().toString();
        return PaymentResult.confirmed(simulatedTransactionId, "Debit card payment processed successfully (simulated).");
    }
}
