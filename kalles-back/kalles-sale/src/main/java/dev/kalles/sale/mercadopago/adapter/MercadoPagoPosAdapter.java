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

        JsonObject payload = new JsonObject();
        payload.addProperty("name", caixa.name());
        payload.addProperty("fixed_amount", false); // Business Invariant defined in features
        payload.addProperty("store_id", company.mpStoreId());
        payload.addProperty("external_store_id", company.id().toString());
        payload.addProperty("external_id", caixa.id());

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        System.out.println(">>> [MercadoPagoPosAdapter] MP Request Payload: " + payload.toString());

        MPRequest request = MPRequest.builder()
                .uri("/pos")
                .method(HttpMethod.POST)
                .headers(headers)
                .payload(payload)
                .build();

        try {
            MPResponse response = httpClient.send(request);
            System.out.println(">>> [MercadoPagoPosAdapter] MP Response Code: " + response.getStatusCode());
            System.out.println(">>> [MercadoPagoPosAdapter] MP Response Body: " + response.getContent());

            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                throw new MercadoPagoIntegrationException("Fail to create MP POS. HTTP Status: "
                        + response.getStatusCode() + " - " + response.getContent());
            }

            JsonObject responseJson = com.google.gson.JsonParser.parseString(response.getContent()).getAsJsonObject();
            if (!responseJson.has("id")) {
                throw new MercadoPagoIntegrationException("SDK returned POS without id");
            }

            Long posId = responseJson.get("id").getAsLong();
            return posId;

        } catch (MPException e) {
            System.err.println(">>> [MercadoPagoPosAdapter] General Exception: " + e.getMessage());
            throw new MercadoPagoIntegrationException("Fail to create MP POS: " + e.getMessage(), e);
        } catch (MPApiException e) {
            System.err.println(">>> [MercadoPagoPosAdapter] API Exception: " + e.getMessage());
            if (e.getApiResponse() != null) {
                System.err.println(">>> [MercadoPagoPosAdapter] API Error Details: " + e.getApiResponse().getContent());
            }
            throw new MercadoPagoIntegrationException("Fail to create MP POS: " + e.getMessage(), e);
        }
    }
}
