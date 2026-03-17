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

    private final MPHttpClient httpClient;
    private final String userId;
    private final String accessToken;

    public MercadoPagoStoreAdapter(MPHttpClient httpClient,
            @Value("${mercadopago.user-id:me}") String userId,
            @Value("${mercadopago.access-token}") String accessToken) {
        this.httpClient = httpClient;
        this.userId = userId;
        this.accessToken = accessToken;
    }

    @Override
    public Long createStore(Company company) {
        if (company.hasStoreRegistered()) {
            return company.mpStoreId();
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
        payload.addProperty("external_id", company.id().toString());
        payload.add("location", location);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        System.out.println(">>> [MercadoPagoStoreAdapter] Access Token prefix: " + (accessToken != null && accessToken.length() > 10 ? accessToken.substring(0, 10) : "null-or-short"));
        System.out.println(">>> [MercadoPagoStoreAdapter] Using user-id para montar URL: " + userId);
        System.out.println(">>> [MercadoPagoStoreAdapter] MP Request Payload: " + payload.toString());

        MPRequest request = MPRequest.builder()
                .uri("/users/" + userId + "/stores")
                .method(HttpMethod.POST)
                .headers(headers)
                .payload(payload)
                .build();

        try {
            System.out.println(">>> [MercadoPagoStoreAdapter] Sending request to MP API: " + request.getUri());
            MPResponse response = httpClient.send(request);
            System.out.println(">>> [MercadoPagoStoreAdapter] MP Response Code: " + response.getStatusCode());
            System.out.println(">>> [MercadoPagoStoreAdapter] MP Response Body: " + response.getContent());

            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                throw new MercadoPagoIntegrationException("Fail to create MP Store. HTTP Status: "
                        + response.getStatusCode() + " - " + response.getContent());
            }

            JsonObject responseJson = JsonParser.parseString(response.getContent()).getAsJsonObject();
            if (!responseJson.has("id")) {
                throw new MercadoPagoIntegrationException("SDK returned Store without id");
            }

            Long storeId = responseJson.get("id").getAsLong();
            return storeId;

        } catch (MPException e) {
            System.err.println(">>> [MercadoPagoStoreAdapter] General Exception: " + e.getMessage());
            throw new MercadoPagoIntegrationException("Fail to create MP Store: " + e.getMessage(), e);
        } catch (MPApiException e) {
            System.err.println(">>> [MercadoPagoStoreAdapter] API Exception: " + e.getMessage());
            if (e.getApiResponse() != null) {
                System.err.println(">>> [MercadoPagoStoreAdapter] API Error Details (Content): " + e.getApiResponse().getContent());
            }
            throw new MercadoPagoIntegrationException("Fail to create MP Store: " + e.getMessage(), e);
        }
    }
}
