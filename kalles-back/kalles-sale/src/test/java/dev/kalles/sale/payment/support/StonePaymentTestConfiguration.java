package dev.kalles.sale.payment.support;

import dev.kalles.sale.core.service.PaymentService;
import dev.kalles.sale.payment.adapter.out.stone.StoneWebClient;
import dev.kalles.sale.payment.application.port.in.ProcessPaymentWebhookUseCase;
import dev.kalles.sale.payment.adapter.out.stone.StonePaymentWebhookAdapter;
import dev.kalles.sale.payment.domain.PaymentProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class StonePaymentTestConfiguration {

    @Bean
    StoneProviderStub stoneProviderStub() {
        return new StoneProviderStub(new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @Bean
    StoneWebhookEventProbe stoneWebhookEventProbe() {
        return new StoneWebhookEventProbe();
    }

    @Bean
    @Primary
    ProcessPaymentWebhookUseCase processPaymentWebhookUseCase(StoneWebhookEventProbe probe) {
        StonePaymentWebhookAdapter adapter = new StonePaymentWebhookAdapter("test-webhook-secret");
        return new ProcessPaymentWebhookUseCase() {
            @Override
            public boolean validateSignature(PaymentProvider provider, String xSignature, String xRequestId, String dataId) {
                return provider == PaymentProvider.STONE;
            }

            @Override
            public boolean execute(PaymentProvider provider, java.util.Map<String, Object> payload) {
                if (provider != PaymentProvider.STONE) {
                    return false;
                }
                var event = adapter.parseEvent(payload);
                if (event == null) {
                    return false;
                }
                probe.onEvent(event);
                return true;
            }
        };
    }

    @Bean
    @Primary
    PaymentService paymentService() {
        return Mockito.mock(PaymentService.class);
    }

    @Bean
    @Primary
    StoneWebClient stoneWebClient(StoneProviderStub providerStub) {
        return new StoneWebClient() {
            @Override
            public org.springframework.http.ResponseEntity<String> exchange(
                    HttpMethod method,
                    String url,
                    String body,
                    java.util.Map<String, String> headers
            ) {
                return providerStub.handle(method, url, body, headers);
            }
        };
    }

    @Bean
    @Order(0)
    SecurityFilterChain paymentApiOpenSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/payments/**", "/api/webhooks/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
