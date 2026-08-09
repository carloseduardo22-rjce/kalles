package dev.kalles.payment.adapter.out.stone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class StoneCredentialsResolver {

    private final String baseUrl;
    private final String secretKey;
    private final String serviceRefererName;

    public StoneCredentialsResolver(
            @Value("${stone.api.base-url:https://api.pagar.me}") String baseUrl,
            @Value("${stone.secret-key:}") String secretKey,
            @Value("${stone.service-referer-name:}") String serviceRefererName
    ) {
        this.baseUrl = baseUrl;
        this.secretKey = secretKey;
        this.serviceRefererName = serviceRefererName;
    }

    public String baseUrl() {
        return trimTrailingSlash(baseUrl);
    }

    public String authorizationHeader() {
        requireConfigured(secretKey, "stone.secret-key");
        String token = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    public String serviceRefererName() {
        requireConfigured(serviceRefererName, "stone.service-referer-name");
        return serviceRefererName;
    }

    private void requireConfigured(String value, String property) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Missing required Stone configuration: " + property);
        }
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "https://api.pagar.me";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
