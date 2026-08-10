package dev.kalles.payment.application.service;

import dev.kalles.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.payment.application.port.out.PaymentPointPort;
import dev.kalles.payment.application.port.out.PaymentProviderAccountPort;
import dev.kalles.payment.application.port.out.PaymentStorePort;
import dev.kalles.payment.application.port.out.PaymentTerminalPort;
import dev.kalles.payment.application.port.out.PaymentWebhookPort;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.exception.PaymentProviderNotSupportedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentProviderPortFactoryTest {

    @Test
    void shouldResolvePortsForRegisteredProvider() {
        PaymentGatewayPort gatewayPort = mock(PaymentGatewayPort.class);
        PaymentProviderAccountPort providerAccountPort = mock(PaymentProviderAccountPort.class);
        PaymentStorePort storePort = mock(PaymentStorePort.class);
        PaymentPointPort pointPort = mock(PaymentPointPort.class);
        PaymentTerminalPort terminalPort = mock(PaymentTerminalPort.class);
        PaymentWebhookPort webhookPort = mock(PaymentWebhookPort.class);

        when(gatewayPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(providerAccountPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(storePort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(pointPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(terminalPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(webhookPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);

        PaymentProviderPortFactory factory = new PaymentProviderPortFactory(
                List.of(gatewayPort),
                List.of(providerAccountPort),
                List.of(storePort),
                List.of(pointPort),
                List.of(terminalPort),
                List.of(webhookPort)
        );

        assertThat(factory.gateway(PaymentProvider.MERCADO_PAGO)).isSameAs(gatewayPort);
        assertThat(factory.providerAccount(PaymentProvider.MERCADO_PAGO)).isSameAs(providerAccountPort);
        assertThat(factory.store(PaymentProvider.MERCADO_PAGO)).isSameAs(storePort);
        assertThat(factory.point(PaymentProvider.MERCADO_PAGO)).isSameAs(pointPort);
        assertThat(factory.terminal(PaymentProvider.MERCADO_PAGO)).isSameAs(terminalPort);
        assertThat(factory.webhook(PaymentProvider.MERCADO_PAGO)).isSameAs(webhookPort);
    }

    @Test
    void shouldRejectProvidersWithoutRegisteredPorts() {
        PaymentProviderAccountPort providerAccountPort = mock(PaymentProviderAccountPort.class);
        PaymentStorePort storePort = mock(PaymentStorePort.class);
        PaymentPointPort pointPort = mock(PaymentPointPort.class);
        PaymentTerminalPort terminalPort = mock(PaymentTerminalPort.class);
        PaymentWebhookPort webhookPort = mock(PaymentWebhookPort.class);

        when(providerAccountPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(storePort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(pointPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(terminalPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);
        when(webhookPort.provider()).thenReturn(PaymentProvider.MERCADO_PAGO);

        PaymentProviderPortFactory factory = new PaymentProviderPortFactory(
                List.<PaymentGatewayPort>of(),
                List.of(providerAccountPort),
                List.of(storePort),
                List.of(pointPort),
                List.of(terminalPort),
                List.of(webhookPort)
        );

        assertThatThrownBy(() -> factory.gateway(PaymentProvider.MERCADO_PAGO))
                .isInstanceOf(PaymentProviderNotSupportedException.class)
                .hasMessageContaining("MERCADO_PAGO")
                .hasMessageContaining("PaymentGatewayPort");
    }
}
