package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.HttpMethod;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.MercadoPagoStorePort;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoStoreAdapter implements MercadoPagoStorePort {

    private final String fallbackUserId;
    private final String fallbackAccessToken;
    private final java.net.http.HttpClient jdkClient;
    private final dev.kalles.sale.mercadopago.port.TenantRepository tenantRepository;

    public MercadoPagoStoreAdapter(
            @Value("${mercadopago.user-id:me}") String fallbackUserId,
            @Value("${mercadopago.access-token}") String fallbackAccessToken,
            java.net.http.HttpClient jdkClient,
            dev.kalles.sale.mercadopago.port.TenantRepository tenantRepository) {
        this.fallbackUserId = fallbackUserId;
        this.fallbackAccessToken = fallbackAccessToken;
        this.jdkClient = jdkClient;
        this.tenantRepository = tenantRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MercadoPagoStoreAdapter(
            @Value("${mercadopago.user-id:me}") String fallbackUserId,
            @Value("${mercadopago.access-token}") String fallbackAccessToken,
            dev.kalles.sale.mercadopago.port.TenantRepository tenantRepository) {
        this(fallbackUserId, fallbackAccessToken, java.net.http.HttpClient.newHttpClient(), tenantRepository);
    }

    private String getAccessToken() {
        return tenantRepository.findById(TenantContextHolder.getTenantId())
                .map(java.util.function.Function.identity())
                .map(dev.kalles.sale.mercadopago.domain.Tenant::mpAccessToken)
                .orElse(null);
    }

    private String getUserId() {
        return tenantRepository.findById(TenantContextHolder.getTenantId())
                .map(java.util.function.Function.identity())
                .map(dev.kalles.sale.mercadopago.domain.Tenant::mpUserId)
                .orElse(null);
    }

    @Override
    public Long createStore(Company company) {
        if (company.hasStoreRegistered()) {
            return company.mpStoreId();
        }

        String extId = company.externalId() != null ? company.externalId().trim() : "";

        try {
            String token = getAccessToken();
            java.net.http.HttpRequest searchReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + getUserId()
                            + "/stores/search?external_id=" + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> searchResp = jdkClient.send(searchReq,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (searchResp.statusCode() == 200) {
                JsonObject searchJson = JsonParser.parseString(searchResp.body()).getAsJsonObject();
                if (searchJson.has("results") && searchJson.get("results").getAsJsonArray().size() > 0) {
                    JsonObject existingStore = searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject();
                    System.out.println(">>> [MercadoPagoStoreAdapter] Store already exists in MP with ID: "
                            + existingStore.get("id").getAsLong());
                    return existingStore.get("id").getAsLong();
                }
            }
        } catch (Exception e) {
            System.err
                    .println(">>> [MercadoPagoStoreAdapter] Failed to search for existing store, proceeding to create: "
                            + e.getMessage());
        }

        JsonObject location = new JsonObject();
        location.addProperty("street_name", company.streetName());
        location.addProperty("street_number", company.streetNumber());
        location.addProperty("city_name", company.cityName());
        location.addProperty("state_name", company.stateName());
        location.addProperty("latitude", company.latitude());
        location.addProperty("longitude", company.longitude());

        JsonObject payload = new JsonObject();
        payload.addProperty("name", company.name());
        payload.addProperty("external_id", extId);
        payload.add("location", location);

        String token = getAccessToken();
        String activeUserId = getUserId();
        if (activeUserId == null || token == null) {
            throw new MercadoPagoIntegrationException("Mercado Pago account is not linked");
        }
        System.out.println(">>> [MercadoPagoStoreAdapter] Access Token prefix: "
                + (token != null && token.length() > 10 ? token.substring(0, 10) : "null-or-short"));
        System.out.println(">>> [MercadoPagoStoreAdapter] Using user-id para montar URL: " + activeUserId);
        System.out.println(">>> [MercadoPagoStoreAdapter] MP Request Payload: " + payload.toString());

        try {
            System.out.println(">>> [MercadoPagoStoreAdapter] Sending request to MP API: /users/" + activeUserId + "/stores");

            // Standardizing with java.net.http.HttpClient to ensure UTF-8 payload encoding
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + activeUserId + "/stores"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString(),
                            java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            System.out.println(">>> [MercadoPagoStoreAdapter] MP Response Code: " + response.statusCode());
            System.out.println(">>> [MercadoPagoStoreAdapter] MP Response Body: " + response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                // If it is 400 Bad Request and already exists, handle it gracefully
                String resBody = response.body();
                if (resBody != null && resBody.contains("is already assigned to this user")) {
                    System.out.println(
                            ">>> [MercadoPagoStoreAdapter] Store already assigned, but search missed it. Re-invoking search.");
                    return searchStoreIdFallback(extId);
                }
                throw new MercadoPagoIntegrationException("Fail to create MP Store. HTTP Status: "
                        + response.statusCode() + " - " + response.body());
            }

            JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!responseJson.has("id")) {
                throw new MercadoPagoIntegrationException("SDK returned Store without id");
            }

            Long storeId = responseJson.get("id").getAsLong();
            return storeId;

        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoStoreAdapter] Exception: " + e.getMessage());
            throw new MercadoPagoIntegrationException("Fail to create MP Store: " + e.getMessage(), e);
        }
    }

    private Long searchStoreIdFallback(String extId) {
        String token = getAccessToken();
        String activeUserId = getUserId();
        if (activeUserId == null || token == null) return null;

        try {
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + activeUserId
                            + "/stores/search?external_id=" + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject searchJson = JsonParser.parseString(response.body()).getAsJsonObject();
                if (searchJson.has("results") && searchJson.get("results").getAsJsonArray().size() > 0) {
                    return searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> fetchStores() {
        String userId = getUserId();
        String accessToken = getAccessToken();
        if (userId == null || accessToken == null) {
            return java.util.Collections.emptyList();
        }

        try {
            java.net.http.HttpRequest searchReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + getUserId() + "/stores/search"))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> searchResp = jdkClient.send(searchReq,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (searchResp.statusCode() == 200) {
                JsonObject searchJson = JsonParser.parseString(searchResp.body()).getAsJsonObject();
                if (searchJson.has("results") && searchJson.get("results").isJsonArray()) {
                    List<Map<String, Object>> stores = new java.util.ArrayList<>();
                    searchJson.get("results").getAsJsonArray().forEach(element -> {
                        JsonObject storeObj = element.getAsJsonObject();
                        Map<String, Object> map = new HashMap<>();
                        if (storeObj.has("id"))
                            map.put("id", storeObj.get("id").getAsLong());
                        if (storeObj.has("name") && !storeObj.get("name").isJsonNull())
                            map.put("name", storeObj.get("name").getAsString());
                        if (storeObj.has("external_id") && !storeObj.get("external_id").isJsonNull())
                            map.put("external_id", storeObj.get("external_id").getAsString());
                        if (storeObj.has("date_creation") && !storeObj.get("date_creation").isJsonNull())
                            map.put("date_creation", storeObj.get("date_creation").getAsString());
                        stores.add(map);
                    });
                    return stores;
                }
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoStoreAdapter] Failed to fetch stores: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
