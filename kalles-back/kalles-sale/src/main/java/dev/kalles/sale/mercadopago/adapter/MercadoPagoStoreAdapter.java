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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MercadoPagoStoreAdapter implements MercadoPagoStorePort {

    private final String userId;
    private final String accessToken;
    private final java.net.http.HttpClient jdkClient;

    public MercadoPagoStoreAdapter(
            @Value("${mercadopago.user-id:me}") String userId,
            @Value("${mercadopago.access-token}") String accessToken,
            java.net.http.HttpClient jdkClient) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.jdkClient = jdkClient;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MercadoPagoStoreAdapter(
            @Value("${mercadopago.user-id:me}") String userId,
            @Value("${mercadopago.access-token}") String accessToken) {
        this(userId, accessToken, java.net.http.HttpClient.newHttpClient());
    }

    @Override
    public Long createStore(Company company) {
        if (company.hasStoreRegistered()) {
            return company.mpStoreId();
        }

        String extId = company.externalId() != null ? company.externalId().trim() : "";

        try {
            java.net.http.HttpRequest searchReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + userId + "/stores/search?external_id=" + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> searchResp = jdkClient.send(searchReq, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (searchResp.statusCode() == 200) {
                JsonObject searchJson = JsonParser.parseString(searchResp.body()).getAsJsonObject();
                if (searchJson.has("results") && searchJson.get("results").getAsJsonArray().size() > 0) {
                    JsonObject existingStore = searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject();
                    System.out.println(">>> [MercadoPagoStoreAdapter] Store already exists in MP with ID: " + existingStore.get("id").getAsLong());
                    return existingStore.get("id").getAsLong();
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoStoreAdapter] Failed to search for existing store, proceeding to create: " + e.getMessage());
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

        System.out.println(">>> [MercadoPagoStoreAdapter] Access Token prefix: " + (accessToken != null && accessToken.length() > 10 ? accessToken.substring(0, 10) : "null-or-short"));
        System.out.println(">>> [MercadoPagoStoreAdapter] Using user-id para montar URL: " + userId);
        System.out.println(">>> [MercadoPagoStoreAdapter] MP Request Payload: " + payload.toString());

        try {
            System.out.println(">>> [MercadoPagoStoreAdapter] Sending request to MP API: /users/" + userId + "/stores");
            
            // Standardizing with java.net.http.HttpClient to ensure UTF-8 payload encoding
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + userId + "/stores"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();
            
            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            System.out.println(">>> [MercadoPagoStoreAdapter] MP Response Code: " + response.statusCode());
            System.out.println(">>> [MercadoPagoStoreAdapter] MP Response Body: " + response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                // If it is 400 Bad Request and already exists, handle it gracefully
                String resBody = response.body();
                if (resBody != null && resBody.contains("is already assigned to this user")) {
                    System.out.println(">>> [MercadoPagoStoreAdapter] Store already assigned, but search missed it. Re-invoking search.");
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
        try {
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/users/" + userId + "/stores/search?external_id=" + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject searchJson = JsonParser.parseString(response.body()).getAsJsonObject();
                if (searchJson.has("results") && searchJson.get("results").getAsJsonArray().size() > 0) {
                    return searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
}
