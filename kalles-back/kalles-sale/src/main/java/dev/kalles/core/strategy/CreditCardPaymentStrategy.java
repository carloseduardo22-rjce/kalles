package dev.kalles.core.strategy;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.kalles.core.enums.payment.PaymentMethod;

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private final boolean simulationEnabled;

    public CreditCardPaymentStrategy(@Value("${payments.simulation.enabled:false}") boolean simulationEnabled) {
        this.simulationEnabled = simulationEnabled;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult process(BigDecimal amount) {
        if (!simulationEnabled) {
            // Sem integração real neste fluxo: confirmar aqui marcaria a venda como
            // paga sem nenhuma cobrança. Cartão deve ser cobrado via terminal
            // integrado (Mercado Pago/Stone), confirmado por webhook.
            throw new IllegalStateException(
                    "Pagamento com cartão de crédito indisponível neste fluxo. Utilize o terminal integrado.");
        }
        String simulatedTransactionId = UUID.randomUUID().toString();
        return PaymentResult.confirmed(simulatedTransactionId, "Credit card payment processed successfully (simulated).");
    }
}
