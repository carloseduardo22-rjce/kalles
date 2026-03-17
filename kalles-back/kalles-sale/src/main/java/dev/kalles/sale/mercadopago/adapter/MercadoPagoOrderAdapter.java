package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.HttpMethod;
import com.mercadopago.net.MPHttpClient;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MercadoPagoOrderAdapter implements MercadoPagoOrderPort {

    private final MPHttpClient httpClient;
    private final CaixaMpRepository caixaMpRepository;

    public MercadoPagoOrderAdapter(MPHttpClient httpClient, CaixaMpRepository caixaMpRepository) {
        this.httpClient = httpClient;
        this.caixaMpRepository = caixaMpRepository;
    }

    @Override
    public ResultadoQr createOrder(CobrancaQr cobranca) {
        Caixa caixa = caixaMpRepository.findById(cobranca.caixaExternalId())
                .orElseThrow(() -> new IllegalStateException("Caixa not found: " + cobranca.caixaExternalId()));

        if (!caixa.hasPosRegistered()) {
            throw new IllegalStateException("Caixa " + caixa.id() + " does not have a registered MP POS");
        }

        JsonObject qrConfig = new JsonObject();
        qrConfig.addProperty("external_pos_id", caixa.id());
        qrConfig.addProperty("mode", "dynamic"); // Business invariant for QR Code type

        JsonObject config = new JsonObject();
        config.add("qr", qrConfig);

        JsonObject paymentReq = new JsonObject();
        paymentReq.addProperty("amount", cobranca.amount());

        JsonArray paymentsArray = new JsonArray();
        paymentsArray.add(paymentReq);

        JsonObject transactionsReq = new JsonObject();
        transactionsReq.add("payments", paymentsArray);

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "qr");
        payload.addProperty("total_amount", cobranca.amount());
        payload.addProperty("external_reference", cobranca.orderIdErp()); // Reference tying to ERP order
        payload.add("config", config);
        payload.add("transactions", transactionsReq);

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("X-Idempotency-Key", cobranca.idempotencyKey());
        customHeaders.put("Content-Type", "application/json");

        MPRequest request = MPRequest.builder()
                .uri("/v1/orders")
                .method(HttpMethod.POST)
                .headers(customHeaders)
                .payload(payload)
                .build();

        try {
            MPResponse response = httpClient.send(request);

            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                throw new MercadoPagoIntegrationException("Fail to create MP Order. HTTP Status: "
                        + response.getStatusCode() + " - " + response.getContent());
            }

            JsonObject responseJson = com.google.gson.JsonParser.parseString(response.getContent()).getAsJsonObject();
            if (!responseJson.has("id") || !responseJson.has("type_response")) {
                throw new MercadoPagoIntegrationException("SDK returned Order without ID or type_response");
            }

            JsonObject typeResponse = responseJson.getAsJsonObject("type_response");
            if (!typeResponse.has("qr_data")) {
                throw new MercadoPagoIntegrationException("SDK returned Order without qr_data");
            }

            return new ResultadoQr(responseJson.get("id").getAsString(), typeResponse.get("qr_data").getAsString());

        } catch (MPException e) {
            System.err.println(">>> [MercadoPagoOrderAdapter] General Exception: " + e.getMessage());
            throw new MercadoPagoIntegrationException("Fail to generate dynamic QR: " + e.getMessage(), e);
        } catch (MPApiException e) {
            System.err.println(">>> [MercadoPagoOrderAdapter] API Exception: " + e.getMessage());
            if (e.getApiResponse() != null) {
                System.err.println(">>> [MercadoPagoOrderAdapter] API Error Details: " + e.getApiResponse().getContent());
            }
            throw new MercadoPagoIntegrationException("Fail to generate dynamic QR: " + e.getMessage(), e);
        }
    }
}
