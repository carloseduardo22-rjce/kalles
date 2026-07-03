package dev.kalles.sale.core.strategy;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.kalles.sale.core.enums.payment.PaymentMethod;

@Component
public class PixPaymentStrategy implements PaymentStrategy {

    private final boolean simulationEnabled;

    public PixPaymentStrategy(@Value("${payments.simulation.enabled:false}") boolean simulationEnabled) {
        this.simulationEnabled = simulationEnabled;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.PIX;
    }

    @Override
    public PaymentResult process(BigDecimal amount) {
        if (!simulationEnabled) {
            // Sem integração real neste fluxo: confirmar aqui marcaria a venda como
            // paga sem nenhuma cobrança real de PIX.
            throw new IllegalStateException(
                    "Pagamento PIX indisponível neste fluxo. Utilize o terminal integrado.");
        }
        String simulatedTransactionId = UUID.randomUUID().toString();
        return PaymentResult.confirmed(simulatedTransactionId, "PIX payment processed successfully (simulated).");
    }
}
