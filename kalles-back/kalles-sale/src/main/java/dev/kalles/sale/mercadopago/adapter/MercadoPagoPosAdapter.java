package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.HttpMethod;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.Company;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.MercadoPagoPosPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MercadoPagoPosAdapter implements MercadoPagoPosPort {

    private final MPHttpClient httpClient;
    private final String accessToken;

    public MercadoPagoPosAdapter(MPHttpClient httpClient,
            @Value("${mercadopago.access-token}") String accessToken) {
        this.httpClient = httpClient;
        this.accessToken = accessToken;
    }

    @Override
    public Long createPos(Caixa caixa, Company company) {
        if (caixa.hasPosRegistered()) {
            return caixa.mpPosId();
        }

        if (!company.hasStoreRegistered()) {
            throw new IllegalStateException("Company " + company.id() + " does not have a registered MP Store");
        }

        String extId = caixa.externalId() != null ? caixa.externalId().trim() : "";

        try {
            java.net.http.HttpClient jdkClient = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest searchReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos?external_id=" + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> searchResp = jdkClient.send(searchReq, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (searchResp.statusCode() == 200) {
                JsonObject searchJson = com.google.gson.JsonParser.parseString(searchResp.body()).getAsJsonObject();
                if (searchJson.has("paging") && searchJson.get("paging").getAsJsonObject().get("total").getAsInt() > 0) {
                    JsonObject existingPos = searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject();
                    System.out.println(">>> [MercadoPagoPosAdapter] POS already exists in MP with ID: " + existingPos.get("id").getAsLong());
                    return existingPos.get("id").getAsLong();
                } else if (searchJson.has("id")) { // In case it returns single dict instead of array
                    return searchJson.get("id").getAsLong();
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoPosAdapter] Failed to search for existing POS, proceeding to create: " + e.getMessage());
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("name", caixa.name());
        payload.addProperty("fixed_amount", false); // Business Invariant defined in features
        payload.addProperty("store_id", company.mpStoreId());
        payload.addProperty("external_store_id", company.externalId());
        payload.addProperty("external_id", extId);

        System.out.println(">>> [MercadoPagoPosAdapter] MP Request Payload: " + payload.toString());

        try {
            System.out.println(">>> [MercadoPagoPosAdapter] Sending request to MP API: /pos");

            // Standardizing with java.net.http.HttpClient to ensure UTF-8 payload encoding
            java.net.http.HttpClient jdkClient = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println(">>> [MercadoPagoPosAdapter] MP Response Code: " + response.statusCode());
            System.out.println(">>> [MercadoPagoPosAdapter] MP Response Body: " + response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String resBody = response.body();
                if (resBody != null && resBody.contains("is already assigned")) {
                    System.out.println(">>> [MercadoPagoPosAdapter] POS already assigned, but search missed it. Re-invoking search.");
                    return searchPosIdFallback(extId);
                }
                throw new MercadoPagoIntegrationException("Fail to create MP POS. HTTP Status: "
                        + response.statusCode() + " - " + response.body());
            }

            JsonObject responseJson = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
            if (!responseJson.has("id")) {
                throw new MercadoPagoIntegrationException("SDK returned POS without id");
            }

            return responseJson.get("id").getAsLong();

        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoPosAdapter] Exception: " + e.getMessage());
            throw new MercadoPagoIntegrationException("Fail to create MP POS: " + e.getMessage(), e);
        }
    }

    private Long searchPosIdFallback(String extId) {
        try {
            java.net.http.HttpClient jdkClient = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos?external_id=" + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject searchJson = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                if (searchJson.has("paging") && searchJson.get("paging").getAsJsonObject().get("total").getAsInt() > 0) {
                    return searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
}
