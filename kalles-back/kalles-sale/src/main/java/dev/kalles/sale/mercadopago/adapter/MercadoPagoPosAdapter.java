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
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoPosAdapter implements MercadoPagoPosPort {

    private final java.net.http.HttpClient jdkClient;
    private final String fallbackAccessToken;
    private final dev.kalles.sale.mercadopago.port.TenantRepository tenantRepository;

    public MercadoPagoPosAdapter(java.net.http.HttpClient jdkClient,
            @Value("${mercadopago.access-token}") String fallbackAccessToken,
            dev.kalles.sale.mercadopago.port.TenantRepository tenantRepository) {
        this.jdkClient = jdkClient;
        this.fallbackAccessToken = fallbackAccessToken;
        this.tenantRepository = tenantRepository;
    }

    private String getAccessToken() {
        return tenantRepository.findById(TenantContextHolder.getTenantId())
                .map(java.util.function.Function.identity())
                .map(dev.kalles.sale.mercadopago.domain.Tenant::mpAccessToken)
                .orElse(null);
    }

    @Override
    public Long createPos(Caixa caixa, Company company) {
        String token = getAccessToken();
        if (token == null) {
            throw new MercadoPagoIntegrationException("Mercado Pago account is not linked");
        }

        if (caixa.hasPosRegistered()) {
            return caixa.mpPosId();
        }

        if (!company.hasStoreRegistered()) {
            throw new IllegalStateException("Company " + company.id() + " does not have a registered MP Store");
        }

        String extId = caixa.externalId() != null ? caixa.externalId().trim() : "";

        try {
            java.net.http.HttpRequest searchReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos?external_id="
                            + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> searchResp = jdkClient.send(searchReq,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (searchResp.statusCode() == 200) {
                JsonObject searchJson = com.google.gson.JsonParser.parseString(searchResp.body()).getAsJsonObject();
                if (searchJson.has("paging")
                        && searchJson.get("paging").getAsJsonObject().get("total").getAsInt() > 0) {
                    JsonObject existingPos = searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject();
                    System.out.println(">>> [MercadoPagoPosAdapter] POS already exists in MP with ID: "
                            + existingPos.get("id").getAsLong());
                    return existingPos.get("id").getAsLong();
                } else if (searchJson.has("id")) { // In case it returns single dict instead of array
                    return searchJson.get("id").getAsLong();
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoPosAdapter] Failed to search for existing POS, proceeding to create: "
                    + e.getMessage());
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

            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString(),
                            java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println(">>> [MercadoPagoPosAdapter] MP Response Code: " + response.statusCode());
            System.out.println(">>> [MercadoPagoPosAdapter] MP Response Body: " + response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String resBody = response.body();
                if (resBody != null && resBody.contains("is already assigned")) {
                    System.out.println(
                            ">>> [MercadoPagoPosAdapter] POS already assigned, but search missed it. Re-invoking search.");
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

            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos?external_id="
                            + java.net.URLEncoder.encode(extId, "UTF-8")))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject searchJson = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                if (searchJson.has("paging")
                        && searchJson.get("paging").getAsJsonObject().get("total").getAsInt() > 0) {
                    return searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> fetchPos() {
        String token = getAccessToken();
        if (token == null) {
            return new java.util.ArrayList<>();
        }

        try {
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/pos"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> resp = jdkClient.send(req,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(resp.body()).getAsJsonObject();
                if (json.has("paging") && json.get("paging").getAsJsonObject().get("total").getAsInt() > 0) {
                    List<Map<String, Object>> posList = new java.util.ArrayList<>();
                    json.get("results").getAsJsonArray().forEach(element -> {
                        com.google.gson.JsonObject posObj = element.getAsJsonObject();
                        Map<String, Object> map = new java.util.HashMap<>();
                        if (posObj.has("id"))
                            map.put("id", posObj.get("id").getAsLong());
                        if (posObj.has("name") && !posObj.get("name").isJsonNull())
                            map.put("name", posObj.get("name").getAsString());
                        if (posObj.has("store_id") && !posObj.get("store_id").isJsonNull())
                            map.put("store_id", posObj.get("store_id").getAsLong());
                        if (posObj.has("external_id") && !posObj.get("external_id").isJsonNull())
                            map.put("external_id", posObj.get("external_id").getAsString());
                        if (posObj.has("status") && !posObj.get("status").isJsonNull())
                            map.put("status", posObj.get("status").getAsString());
                        if (posObj.has("date_created") && !posObj.get("date_created").isJsonNull())
                            map.put("date_created", posObj.get("date_created").getAsString());
                        if (posObj.has("date_last_updated") && !posObj.get("date_last_updated").isJsonNull())
                            map.put("date_last_updated", posObj.get("date_last_updated").getAsString());
                        posList.add(map);
                    });
                    return posList;
                }
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoPosAdapter] Failed to fetch POS: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
