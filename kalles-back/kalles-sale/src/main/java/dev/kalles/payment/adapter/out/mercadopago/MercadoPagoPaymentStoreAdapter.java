package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.payment.adapter.out.mercadopago.dto.StoreCreateRequest;
import dev.kalles.payment.adapter.out.mercadopago.dto.StoreResponse;
import dev.kalles.payment.adapter.out.mercadopago.dto.StoreSearchResponse;
import dev.kalles.payment.application.port.out.PaymentStorePort;
import dev.kalles.payment.domain.MerchantProfile;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStore;
import dev.kalles.payment.domain.PaymentStoreView;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoPaymentStoreAdapter implements PaymentStorePort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoPaymentStoreAdapter(
            MercadoPagoCredentialsResolver credentialsResolver,
            MercadoPagoWebClient mercadoPagoWebClient,
            ObjectMapper objectMapper
    ) {
        this.credentialsResolver = credentialsResolver;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public PaymentStore createStore(PaymentStore store, MerchantProfile merchantProfile) {
        if (store.hasProviderStore()) {
            return store;
        }

        String extId = store.externalReference() != null ? store.externalReference().trim() : "";
        String token = credentialsResolver.linkedAccessTokenOrThrow();
        String userId = credentialsResolver.linkedUserIdOrThrow();

        String existingStoreId = searchStoreId(extId, token, userId);
        if (existingStoreId != null) {
            return store.withProviderStoreId(existingStoreId);
        }

        StoreCreateRequest payload = new StoreCreateRequest(
                merchantProfile.name() != null ? merchantProfile.name() : "",
                extId,
                new StoreCreateRequest.Location(
                        merchantProfile.streetName() != null ? merchantProfile.streetName() : "",
                        merchantProfile.streetNumber() != null ? merchantProfile.streetNumber() : "S/N",
                        merchantProfile.cityName() != null ? merchantProfile.cityName() : "S/I",
                        merchantProfile.stateName() != null ? merchantProfile.stateName() : "S/I",
                        merchantProfile.latitude(),
                        merchantProfile.longitude()
                )
        );

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/users/" + userId + "/stores",
                    objectMapper.writeValueAsString(payload),
                    Map.of(
                            "Authorization", "Bearer " + token,
                            "Content-Type", "application/json; charset=UTF-8"
                    )
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("is already assigned to this user")) {
                    String fallbackStoreId = searchStoreId(extId, token, userId);
                    if (fallbackStoreId != null) {
                        return store.withProviderStoreId(fallbackStoreId);
                    }
                }
                throw new MercadoPagoAdapterException("Fail to create MP Store. HTTP Status: "
                        + response.getStatusCode().value() + " - " + responseBody);
            }

            StoreResponse createdStore = objectMapper.readValue(response.getBody(), StoreResponse.class);
            if (createdStore.id() == null) {
                throw new MercadoPagoAdapterException("SDK returned Store without id");
            }

            return store.withProviderStoreId(createdStore.id());
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to create MP Store: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PaymentStoreView> listStores() {
        String userId = credentialsResolver.linkedUserIdOrThrow();
        String accessToken = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/users/" + userId + "/stores/search",
                    null,
                    Map.of("Authorization", "Bearer " + accessToken)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return List.of();
            }

            StoreSearchResponse searchResponse = objectMapper.readValue(response.getBody(), StoreSearchResponse.class);
            if (searchResponse.results() == null) {
                return List.of();
            }

            return searchResponse.results().stream()
                    .map(store -> new PaymentStoreView(
                            store.id(),
                            store.name(),
                            store.externalId(),
                            store.dateCreation(),
                            Map.of()
                    ))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String searchStoreId(String externalId, String token, String userId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/users/" + userId + "/stores/search?external_id="
                            + URLEncoder.encode(externalId, StandardCharsets.UTF_8),
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            StoreSearchResponse searchResponse = objectMapper.readValue(response.getBody(), StoreSearchResponse.class);
            if (searchResponse.results() == null || searchResponse.results().isEmpty()) {
                return null;
            }

            return searchResponse.results().getFirst().id();
        } catch (Exception e) {
            return null;
        }
    }
}
