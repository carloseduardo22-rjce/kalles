package dev.kalles.sale.mercadopago.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.kalles.sale.mercadopago.domain.Caixa;
import dev.kalles.sale.mercadopago.domain.CobrancaQr;
import dev.kalles.sale.mercadopago.domain.ResultadoQr;
import dev.kalles.sale.mercadopago.exception.MercadoPagoIntegrationException;
import dev.kalles.sale.mercadopago.port.CaixaMpRepository;
import dev.kalles.sale.mercadopago.port.MercadoPagoOrderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoOrderAdapter implements MercadoPagoOrderPort {

    private final java.net.http.HttpClient jdkClient;
    private final String accessToken;
    private final CaixaMpRepository caixaMpRepository;

    public MercadoPagoOrderAdapter(
            @Value("${mercadopago.access-token}") String accessToken,
            java.net.http.HttpClient jdkClient,
            CaixaMpRepository caixaMpRepository) {
        this.accessToken = accessToken;
        this.jdkClient = jdkClient;
        this.caixaMpRepository = caixaMpRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MercadoPagoOrderAdapter(
            @Value("${mercadopago.access-token}") String accessToken,
            CaixaMpRepository caixaMpRepository) {
        this(accessToken, java.net.http.HttpClient.newHttpClient(), caixaMpRepository);
    }

    @Override
    public ResultadoQr createOrder(CobrancaQr cobranca) {
        Caixa caixa = caixaMpRepository.findByExternalId(cobranca.caixaExternalId())
                .orElseThrow(() -> new IllegalStateException("Caixa not found: " + cobranca.caixaExternalId()));

        if (!caixa.hasPosRegistered()) {
            throw new IllegalStateException("Caixa " + caixa.externalId() + " does not have a registered MP POS");
        }

        JsonObject qrConfig = new JsonObject();
        qrConfig.addProperty("external_pos_id", caixa.externalId());
        qrConfig.addProperty("mode", "dynamic"); // Business invariant for QR Code type

        JsonObject config = new JsonObject();
        config.add("qr", qrConfig);

        JsonObject paymentReq = new JsonObject();
        paymentReq.addProperty("amount", cobranca.amount().toPlainString());

        JsonArray paymentsArray = new JsonArray();
        paymentsArray.add(paymentReq);

        JsonObject transactionsReq = new JsonObject();
        transactionsReq.add("payments", paymentsArray);

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "qr");
        payload.addProperty("total_amount", cobranca.amount().toPlainString());
        payload.addProperty("external_reference", cobranca.orderIdErp());
        payload.add("config", config);
        payload.add("transactions", transactionsReq);

        try {
            java.net.http.HttpRequest jdkRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.mercadopago.com/v1/orders"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Idempotency-Key", cobranca.idempotencyKey())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString(), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<String> response = jdkClient.send(jdkRequest, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new MercadoPagoIntegrationException("Fail to create MP Order. HTTP Status: "
                        + response.statusCode() + " - " + response.body());
            }

            JsonObject responseJson = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();

            if (!responseJson.has("id") || !responseJson.has("type_response")) {
                throw new MercadoPagoIntegrationException("SDK returned Order without ID or type_response");
            }

            JsonObject typeResponse = responseJson.getAsJsonObject("type_response");
            if (!typeResponse.has("qr_data")) {
                throw new MercadoPagoIntegrationException("SDK returned Order without qr_data");
            }

            return new ResultadoQr(responseJson.get("id").getAsString(), typeResponse.get("qr_data").getAsString());

        } catch (Exception e) {
            System.err.println(">>> [MercadoPagoOrderAdapter] Exception: " + e.getMessage());
            throw new MercadoPagoIntegrationException("Fail to generate dynamic QR: " + e.getMessage(), e);
        }
    }
}
