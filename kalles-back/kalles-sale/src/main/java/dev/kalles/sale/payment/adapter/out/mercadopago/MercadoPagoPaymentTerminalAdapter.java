package dev.kalles.sale.payment.adapter.out.mercadopago;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.sale.payment.application.port.out.PaymentTerminalPort;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentTerminal;
import dev.kalles.sale.payment.domain.TerminalOperationMode;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.kalles.sale.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toProviderOperationMode;
import static dev.kalles.sale.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toTerminalOperationMode;

@Component
public class MercadoPagoPaymentTerminalAdapter implements PaymentTerminalPort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;

    public MercadoPagoPaymentTerminalAdapter(
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
    public List<PaymentTerminal> listTerminals(String storeId, String pointId) {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/terminals/v1/list?limit=50&offset=0&store_id="
                            + URLEncoder.encode(storeId, StandardCharsets.UTF_8)
                            + "&pos_id="
                            + URLEncoder.encode(pointId, StandardCharsets.UTF_8),
                    null,
                    Map.of("Authorization", "Bearer " + token)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                return List.of();
            }

            JsonObject searchJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (!searchJson.has("terminals") || searchJson.get("terminals").getAsJsonArray().isEmpty()) {
                return List.of();
            }

            JsonArray terminalsJson = searchJson.get("terminals").getAsJsonArray();
            List<PaymentTerminal> terminals = new ArrayList<>();
            for (JsonElement element : terminalsJson) {
                JsonObject terminalJson = element.getAsJsonObject();
                terminals.add(new PaymentTerminal(
                        getAsString(terminalJson, "id"),
                        valueOrFallback(terminalJson, "pos_id", "posId"),
                        valueOrFallback(terminalJson, "store_id", "storeId"),
                        valueOrFallback(terminalJson, "external_pos_id", "externalPosId"),
                        toTerminalOperationMode(valueOrFallback(terminalJson, "operating_mode", "operationMode"))
                ));
            }
            return terminals;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public boolean changeOperationMode(String terminalId, TerminalOperationMode operationMode) {
        String token = credentialsResolver.linkedAccessTokenOrThrow();

        try {
            JsonObject terminalObj = new JsonObject();
            terminalObj.addProperty("id", terminalId);
            terminalObj.addProperty("operating_mode", toProviderOperationMode(operationMode));

            JsonArray terminalsArray = new JsonArray();
            terminalsArray.add(terminalObj);

            JsonObject payload = new JsonObject();
            payload.add("terminals", terminalsArray);

            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.PATCH,
                    "https://api.mercadopago.com/terminals/v1/setup",
                    payload.toString(),
                    Map.of(
                            "Authorization", "Bearer " + token,
                            "Content-Type", "application/json"
                    )
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private String valueOrFallback(JsonObject jsonObject, String firstOption, String fallbackOption) {
        String first = getAsString(jsonObject, firstOption);
        return first != null ? first : getAsString(jsonObject, fallbackOption);
    }

    private String getAsString(JsonObject jsonObject, String memberName) {
        return jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()
                ? jsonObject.get(memberName).getAsString()
                : null;
    }
}
