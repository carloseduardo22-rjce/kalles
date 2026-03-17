package dev.kalles.sale.mercadopago.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.net.MPHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access-token:TEST-TOKEN-PLACEHOLDER}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Bean
    public MPHttpClient mpHttpClient() {
        return MercadoPagoConfig.getHttpClient();
    }
}
