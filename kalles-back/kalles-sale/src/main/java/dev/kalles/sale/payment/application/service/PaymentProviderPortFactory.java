package dev.kalles.sale.payment.application.service;

import dev.kalles.sale.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.sale.payment.application.port.out.PaymentPointPort;
import dev.kalles.sale.payment.application.port.out.PaymentProviderAccountPort;
import dev.kalles.sale.payment.application.port.out.PaymentStorePort;
import dev.kalles.sale.payment.application.port.out.PaymentTerminalPort;
import dev.kalles.sale.payment.application.port.out.PaymentWebhookPort;
import dev.kalles.sale.payment.application.port.out.ProviderAwarePort;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.exception.PaymentProviderNotSupportedException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderPortFactory {

    private final Map<PaymentProvider, PaymentGatewayPort> gatewayPorts;
    private final Map<PaymentProvider, PaymentProviderAccountPort> providerAccountPorts;
    private final Map<PaymentProvider, PaymentStorePort> storePorts;
    private final Map<PaymentProvider, PaymentPointPort> pointPorts;
    private final Map<PaymentProvider, PaymentTerminalPort> terminalPorts;
    private final Map<PaymentProvider, PaymentWebhookPort> webhookPorts;

    public PaymentProviderPortFactory(
            List<PaymentGatewayPort> gatewayPorts,
            List<PaymentProviderAccountPort> providerAccountPorts,
            List<PaymentStorePort> storePorts,
            List<PaymentPointPort> pointPorts,
            List<PaymentTerminalPort> terminalPorts,
            List<PaymentWebhookPort> webhookPorts
    ) {
        this.gatewayPorts = indexByProvider(gatewayPorts, "PaymentGatewayPort");
        this.providerAccountPorts = indexByProvider(providerAccountPorts, "PaymentProviderAccountPort");
        this.storePorts = indexByProvider(storePorts, "PaymentStorePort");
        this.pointPorts = indexByProvider(pointPorts, "PaymentPointPort");
        this.terminalPorts = indexByProvider(terminalPorts, "PaymentTerminalPort");
        this.webhookPorts = indexByProvider(webhookPorts, "PaymentWebhookPort");
    }

    public PaymentGatewayPort gateway(PaymentProvider provider) {
        return resolve(provider, gatewayPorts, "PaymentGatewayPort");
    }

    public PaymentProviderAccountPort providerAccount(PaymentProvider provider) {
        return resolve(provider, providerAccountPorts, "PaymentProviderAccountPort");
    }

    public PaymentStorePort store(PaymentProvider provider) {
        return resolve(provider, storePorts, "PaymentStorePort");
    }

    public PaymentPointPort point(PaymentProvider provider) {
        return resolve(provider, pointPorts, "PaymentPointPort");
    }

    public PaymentTerminalPort terminal(PaymentProvider provider) {
        return resolve(provider, terminalPorts, "PaymentTerminalPort");
    }

    public PaymentWebhookPort webhook(PaymentProvider provider) {
        return resolve(provider, webhookPorts, "PaymentWebhookPort");
    }

    private static <T extends ProviderAwarePort> Map<PaymentProvider, T> indexByProvider(List<T> ports, String portType) {
        EnumMap<PaymentProvider, T> indexed = new EnumMap<>(PaymentProvider.class);
        for (T port : ports) {
            T previous = indexed.put(port.provider(), port);
            if (previous != null) {
                throw new IllegalStateException("Multiple " + portType + " beans registered for provider " + port.provider());
            }
        }
        return Map.copyOf(indexed);
    }

    private static <T> T resolve(PaymentProvider provider, Map<PaymentProvider, T> ports, String portType) {
        T resolved = ports.get(provider);
        if (resolved == null) {
            throw new PaymentProviderNotSupportedException(provider, portType);
        }
        return resolved;
    }
}
