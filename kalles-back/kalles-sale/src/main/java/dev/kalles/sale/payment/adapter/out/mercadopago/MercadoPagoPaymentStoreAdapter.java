package dev.kalles.sale.payment.adapter.out.mercadopago;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.sale.payment.application.port.out.PaymentStorePort;
import dev.kalles.sale.payment.domain.MerchantProfile;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;
import dev.kalles.sale.payment.domain.PaymentStoreView;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoPaymentStoreAdapter implements PaymentStorePort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;

    public MercadoPagoPaymentStoreAdapter(
            MercadoPagoCredentialsResolver credentialsResolver,
            MercadoPagoWebClient mercadoPagoWebClient
    ) {
        this.credentialsResolver = credentialsResolver;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
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

        Long existingStoreId = searchStoreId(extId, token, userId);
        if (existingStoreId != null) {
            return store.withProviderStoreId(String.valueOf(existingStoreId));
        }

        JsonObject location = new JsonObject();
        location.addProperty("street_name", merchantProfile.streetName() != null ? merchantProfile.streetName() : "");
        location.addProperty("street_number", merchantProfile.streetNumber() != null ? merchantProfile.streetNumber() : "S/N");
        location.addProperty("city_name", merchantProfile.cityName() != null ? merchantProfile.cityName() : "S/I");
        location.addProperty("state_name", merchantProfile.stateName() != null ? merchantProfile.stateName() : "S/I");
        location.addProperty("latitude", merchantProfile.latitude());
        location.addProperty("longitude", merchantProfile.longitude());

        JsonObject payload = new JsonObject();
        payload.addProperty("name", merchantProfile.name() != null ? merchantProfile.name() : "");
        payload.addProperty("external_id", extId);
        payload.add("location", location);

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/users/" + userId + "/stores",
                    payload.toString(),
                    Map.of(
                            "Authorization", "Bearer " + token,
                            "Content-Type", "application/json; charset=UTF-8"
                    )
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("is already assigned to this user")) {
                    Long fallbackStoreId = searchStoreId(extId, token, userId);
                    if (fallbackStoreId != null) {
                        return store.withProviderStoreId(String.valueOf(fallbackStoreId));
                    }
                }
                throw new MercadoPagoAdapterException("Fail to create MP Store. HTTP Status: "
                        + response.getStatusCode().value() + " - " + responseBody);
            }

            JsonObject responseJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (!responseJson.has("id")) {
                throw new MercadoPagoAdapterException("SDK returned Store without id");
            }

            return store.withProviderStoreId(String.valueOf(responseJson.get("id").getAsLong()));
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

            JsonObject searchJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (!searchJson.has("results") || !searchJson.get("results").isJsonArray()) {
                return List.of();
            }

            List<PaymentStoreView> stores = new ArrayList<>();
            searchJson.get("results").getAsJsonArray().forEach(element -> {
                JsonObject storeObj = element.getAsJsonObject();
                stores.add(new PaymentStoreView(
                        storeObj.has("id") ? storeObj.get("id").getAsString() : null,
                        getAsString(storeObj, "name"),
                        getAsString(storeObj, "external_id"),
                        getAsString(storeObj, "date_creation"),
                        Map.of()
                ));
            });
            return stores;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Long searchStoreId(String externalId, String token, String userId) {
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

            JsonObject searchJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (searchJson.has("results") && searchJson.get("results").getAsJsonArray().size() > 0) {
                return searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getAsString(JsonObject jsonObject, String memberName) {
        return jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()
                ? jsonObject.get(memberName).getAsString()
                : null;
    }
}
