package dev.kalles.sale.payment.support;

import dev.kalles.sale.core.service.PaymentService;
import dev.kalles.sale.payment.adapter.out.stone.StoneWebClient;
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
