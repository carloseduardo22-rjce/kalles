package dev.kalles.sale.mercadopago.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.sale.mercadopago.domain.Terminal;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.MercadoPagoTerminalPort;
import dev.kalles.sale.mercadopago.port.TenantRepository;
import dev.kalles.sale.mercadopago.port.TerminalRepository;
import dev.kalles.sale.security.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MercadoPagoTerminalAdapter implements MercadoPagoTerminalPort {

    private final HttpClient jdkClient;
    private final TerminalRepository terminalRepository;
    private final TenantRepository tenantRepository;

    public MercadoPagoTerminalAdapter(HttpClient jdkClient,
            @Value("${mercadopago.access-token}") String fallbackAccessToken,
            TerminalRepository terminalRepository,
            TenantRepository tenantRepository) {
        this.jdkClient = jdkClient;
        this.terminalRepository = terminalRepository;
        this.tenantRepository = tenantRepository;
    }

    private String getAccessToken() {
        return tenantRepository.findById(TenantContextHolder.getTenantId())
                .map(java.util.function.Function.identity())
                .map(dev.kalles.sale.mercadopago.domain.Tenant::mpAccessToken)
                .orElse(null);
    }

    @Override
    public List<Terminal> fetchTerminals(UUID storeId, UUID posId) {
        String token = getAccessToken();
        if (token == null) {
            throw new MercadoPagoIntegrationException("Mercado Pago account is not linked");
        }

        try {
            HttpRequest searchReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mercadopago.com/terminals/v1/list?limit=50&offset=0&store_id="
                            + URLEncoder.encode(storeId.toString(), "UTF-8")
                            + "&pos_id="
                            + URLEncoder.encode(posId.toString(), "UTF-8")))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> searchResp = jdkClient.send(searchReq, HttpResponse.BodyHandlers.ofString());
            if (searchResp.statusCode() == 200) {
                JsonObject searchJson = JsonParser.parseString(searchResp.body()).getAsJsonObject();
                if (searchJson.has("terminals") && searchJson.get("terminals").getAsJsonArray().size() > 0) {
                    JsonArray existingPos = searchJson.get("terminals").getAsJsonArray();
                    List<Terminal> terminals = new ArrayList<>();
                    for (JsonElement terminal : existingPos) {
                        String terminalObj = terminal.getAsJsonObject().toString();
                        ObjectMapper mapper = new ObjectMapper();
                        Terminal terminalDto = mapper.readValue(terminalObj, Terminal.class);
                        terminals.add(terminalDto);
                    }
                    terminalRepository.saveAll(terminals);
                    return terminals;
                } else {
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoPosAdapter] Failed to fetch terminals: "
                    + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean changeToPdvMode(String terminalId) {
        String token = getAccessToken();
        if (token == null) {
            throw new MercadoPagoIntegrationException("Mercado Pago account is not linked");
        }

        try {
            JsonObject terminalObj = new JsonObject();
            terminalObj.addProperty("id", terminalId);
            terminalObj.addProperty("operating_mode", "PDV");

            JsonArray terminalsArray = new JsonArray();
            terminalsArray.add(terminalObj);

            JsonObject bodyObj = new JsonObject();
            bodyObj.add("terminals", terminalsArray);

            String requestBody = bodyObj.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mercadopago.com/terminals/v1/setup"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    // Note: using method("PATCH", BodyPublishers.ofString(...)) because standard OpenJDK HttpClient does not support PATCH through a simple convenience method in older versions.
                    // But wait, JDK HttpClient handles PATCH if we just use .method("PATCH", ...)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = jdkClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return true;
            } else {
                System.err.println(">>> [MercadoPagoTerminalAdapter] Failed to change terminal to PDV mode: " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoTerminalAdapter] Exception changing terminal to PDV mode: " + e.getMessage());
            return false;
        }
    }

}
