package dev.kalles.core.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.kalles.core.enums.payment.PaymentMethod;

@Component
public class PaymentFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getPaymentMethod, Function.identity()));
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Payment method not supported: " + method);
        }
        return strategy;
    }
}
