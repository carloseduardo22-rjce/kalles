package dev.kalles.sale.payment.adapter.out.mercadopago;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.sale.payment.application.port.out.PaymentPointPort;
import dev.kalles.sale.payment.domain.PaymentPoint;
import dev.kalles.sale.payment.domain.PaymentPointDescriptor;
import dev.kalles.sale.payment.domain.PaymentPointView;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStore;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoPaymentPointAdapter implements PaymentPointPort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;

    public MercadoPagoPaymentPointAdapter(
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
    public PaymentPoint createPoint(PaymentPoint point, PaymentStore store, PaymentPointDescriptor descriptor) {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        if (point.hasProviderPoint()) {
            return point;
        }

        if (!store.hasProviderStore()) {
            throw new IllegalStateException("Company does not have a Mercado Pago Store configured.");
        }

        String extId = point.externalReference() != null ? point.externalReference().trim() : "";
        Long existingPosId = searchPosId(extId, token);
        if (existingPosId != null) {
            return point.withProviderPointId(String.valueOf(existingPosId));
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("name", descriptor.description() + " - " + descriptor.code());
        payload.addProperty("fixed_amount", false);
        payload.addProperty("store_id", Long.valueOf(store.providerStoreId()));
        payload.addProperty("external_store_id", store.externalReference());
        payload.addProperty("external_id", extId);

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/pos",
                    payload.toString(),
                    Map.of(
                            "Authorization", "Bearer " + token,
                            "Content-Type", "application/json; charset=UTF-8"
                    )
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.contains("is already assigned")) {
                    Long fallbackPosId = searchPosId(extId, token);
                    if (fallbackPosId != null) {
                        return point.withProviderPointId(String.valueOf(fallbackPosId));
                    }
                }
                throw new MercadoPagoAdapterException("Fail to create MP POS. HTTP Status: "
                        + response.getStatusCode().value() + " - " + responseBody);
            }

            JsonObject responseJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (!responseJson.has("id")) {
                throw new MercadoPagoAdapterException("SDK returned POS without id");
            }

            return point.withProviderPointId(String.valueOf(responseJson.get("id").getAsLong()));
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to create MP POS: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PaymentPointView> listPoints() {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/pos",
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return List.of();
            }

            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (!json.has("paging") || json.get("paging").getAsJsonObject().get("total").getAsInt() <= 0) {
                return List.of();
            }

            List<PaymentPointView> points = new ArrayList<>();
            json.get("results").getAsJsonArray().forEach(element -> {
                JsonObject pointObj = element.getAsJsonObject();
                points.add(new PaymentPointView(
                        pointObj.has("id") ? pointObj.get("id").getAsString() : null,
                        getAsString(pointObj, "name"),
                        getAsString(pointObj, "store_id"),
                        getAsString(pointObj, "external_id"),
                        getAsString(pointObj, "external_store_id"),
                        pointObj.has("fixed_amount") && !pointObj.get("fixed_amount").isJsonNull()
                                ? pointObj.get("fixed_amount").getAsBoolean()
                                : null,
                        getAsString(pointObj, "status"),
                        getAsString(pointObj, "date_created"),
                        getAsString(pointObj, "date_last_updated"),
                        Map.of()
                ));
            });
            return points;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Long searchPosId(String externalId, String token) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/pos?external_id=" + URLEncoder.encode(externalId, StandardCharsets.UTF_8),
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            JsonObject searchJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (searchJson.has("paging") && searchJson.get("paging").getAsJsonObject().get("total").getAsInt() > 0) {
                return searchJson.get("results").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
            }
            if (searchJson.has("id")) {
                return searchJson.get("id").getAsLong();
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
