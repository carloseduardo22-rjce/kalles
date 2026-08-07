package dev.kalles.payment.adapter.out.mercadopago;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toPaymentStatus;
import static dev.kalles.payment.adapter.out.mercadopago.MercadoPagoMappingUtils.toProviderPaymentMethodType;

@Component
public class MercadoPagoPaymentGatewayAdapter implements PaymentGatewayPort {

    private final MercadoPagoCredentialsResolver credentialsResolver;
    private final MercadoPagoWebClient mercadoPagoWebClient;
    private final CashRegisterRepository cashRegisterRepository;
    private final PaymentPointRepository paymentPointRepository;

    public MercadoPagoPaymentGatewayAdapter(
            MercadoPagoCredentialsResolver credentialsResolver,
            MercadoPagoWebClient mercadoPagoWebClient,
            CashRegisterRepository cashRegisterRepository,
            PaymentPointRepository paymentPointRepository
    ) {
        this.credentialsResolver = credentialsResolver;
        this.mercadoPagoWebClient = mercadoPagoWebClient;
        this.cashRegisterRepository = cashRegisterRepository;
        this.paymentPointRepository = paymentPointRepository;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public PaymentResult processPayment(PaymentCommand command) {
        return switch (command.flow()) {
            case QR_CODE -> createQrOrder(command);
            case TERMINAL -> createPointOrder(command);
        };
    }

    @Override
    public PaymentResult getPayment(String providerOrderId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.GET,
                    "https://api.mercadopago.com/v1/orders/" + providerOrderId,
                    null,
                    authorizationHeaders(credentialsResolver.fallbackAccessToken())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to get Point order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }

            return toOrderResult(JsonParser.parseString(response.getBody()).getAsJsonObject());
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to get Point order: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String providerOrderId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/orders/" + providerOrderId + "/cancel",
                    null,
                    jsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to cancel Point Order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to cancel Point order: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResult closePaymentOrder(String providerOrderId, PaymentStatus status) {
        if (status != PaymentStatus.CANCELED) {
            throw new IllegalArgumentException("Mercado Pago only supports canceling orders through this operation");
        }
        cancelPayment(providerOrderId);
        return getPayment(providerOrderId);
    }

    @Override
    public void printDocument(String providerOrderId, PaymentDocumentPrintCommand command) {
        throw new IllegalStateException("Mercado Pago does not support document printing through this integration");
    }

    @Override
    public void refundPayment(String providerPaymentId) {
        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/payments/" + providerPaymentId + "/refunds",
                    null,
                    jsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to refund Point payment. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to refund Point payment: " + e.getMessage(), e);
        }
    }

    private PaymentResult createQrOrder(PaymentCommand command) {
        CashRegister cashRegister = cashRegisterRepository.findByCode(command.targetId())
                .orElseThrow(() -> new IllegalArgumentException("Cash Register code not found: " + command.targetId()));

        var point = paymentPointRepository.findByCashRegisterIdAndProvider(cashRegister.getId(), PaymentProvider.MERCADO_PAGO)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Caixa Integration mapping not found for code: " + command.targetId()));

        if (!point.hasProviderPoint()) {
            throw new IllegalStateException("Caixa does not have a Mercado Pago POS configured.");
        }

        JsonObject qrConfig = new JsonObject();
        qrConfig.addProperty("external_pos_id", point.externalReference());
        qrConfig.addProperty("mode", "dynamic");

        JsonObject config = new JsonObject();
        config.add("qr", qrConfig);

        JsonObject paymentReq = new JsonObject();
        paymentReq.addProperty("amount", command.amount().toPlainString());

        JsonArray paymentsArray = new JsonArray();
        paymentsArray.add(paymentReq);

        JsonObject transactionsReq = new JsonObject();
        transactionsReq.add("payments", paymentsArray);

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "qr");
        payload.addProperty("total_amount", command.amount().toPlainString());
        payload.addProperty("external_reference", command.externalReference());
        payload.add("config", config);
        payload.add("transactions", transactionsReq);

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/orders",
                    payload.toString(),
                    idempotentJsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken(), command.idempotencyKey())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to create MP Order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }

            JsonObject responseJson = JsonParser.parseString(response.getBody()).getAsJsonObject();
            if (!responseJson.has("id") || !responseJson.has("type_response")) {
                throw new MercadoPagoAdapterException("SDK returned Order without ID or type_response");
            }

            JsonObject typeResponse = responseJson.getAsJsonObject("type_response");
            if (!typeResponse.has("qr_data")) {
                throw new MercadoPagoAdapterException("SDK returned Order without qr_data");
            }

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("qrData", typeResponse.get("qr_data").getAsString());

            return new PaymentResult(
                    responseJson.get("id").getAsString(),
                    null,
                    PaymentStatus.CREATED,
                    metadata
            );
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to generate dynamic QR: " + e.getMessage(), e);
        }
    }

    private PaymentResult createPointOrder(PaymentCommand command) {
        JsonObject pointConfig = new JsonObject();
        pointConfig.addProperty("terminal_id", command.targetId());
        pointConfig.addProperty("print_on_terminal", "no_ticket");

        JsonObject paymentMethod = new JsonObject();
        paymentMethod.addProperty("default_type", toProviderPaymentMethodType(command.methodType()));

        JsonObject config = new JsonObject();
        config.add("point", pointConfig);
        config.add("payment_method", paymentMethod);

        JsonObject paymentReq = new JsonObject();
        paymentReq.addProperty("amount", command.amount().toPlainString());

        JsonArray paymentsArray = new JsonArray();
        paymentsArray.add(paymentReq);

        JsonObject transactionsReq = new JsonObject();
        transactionsReq.add("payments", paymentsArray);

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "point");
        payload.addProperty("external_reference", command.externalReference());
        payload.addProperty("description", command.description());
        payload.add("config", config);
        payload.add("transactions", transactionsReq);

        try {
            ResponseEntity<String> response = mercadoPagoWebClient.exchange(
                    HttpMethod.POST,
                    "https://api.mercadopago.com/v1/orders",
                    payload.toString(),
                    idempotentJsonAuthorizationHeaders(credentialsResolver.fallbackAccessToken(), command.idempotencyKey())
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MercadoPagoAdapterException("Fail to create Point Order. HTTP Status: "
                        + response.getStatusCode().value() + " - " + response.getBody());
            }

            return toOrderResult(JsonParser.parseString(response.getBody()).getAsJsonObject());
        } catch (Exception e) {
            throw new MercadoPagoAdapterException("Fail to create Point order: " + e.getMessage(), e);
        }
    }

    private PaymentResult toOrderResult(JsonObject responseJson) {
        String paymentId = extractPaymentId(responseJson);
        return new PaymentResult(
                responseJson.get("id").getAsString(),
                paymentId,
                toPaymentStatus(responseJson.has("status") ? responseJson.get("status").getAsString() : null),
                Map.of()
        );
    }

    private String extractPaymentId(JsonObject responseJson) {
        if (responseJson.has("transactions") && responseJson.getAsJsonObject("transactions").has("payments")) {
            JsonArray payments = responseJson.getAsJsonObject("transactions").getAsJsonArray("payments");
            if (!payments.isEmpty()) {
                JsonObject payment = payments.get(0).getAsJsonObject();
                if (payment.has("id") && !payment.get("id").isJsonNull()) {
                    return payment.get("id").getAsString();
                }
            }
        }
        return null;
    }

    private Map<String, String> authorizationHeaders(String accessToken) {
        return Map.of("Authorization", "Bearer " + accessToken);
    }

    private Map<String, String> jsonAuthorizationHeaders(String accessToken) {
        return Map.of(
                "Authorization", "Bearer " + accessToken,
                "Content-Type", "application/json; charset=UTF-8"
        );
    }

    private Map<String, String> idempotentJsonAuthorizationHeaders(String accessToken, String idempotencyKey) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("X-Idempotency-Key", idempotencyKey);
        headers.put("Content-Type", "application/json; charset=UTF-8");
        return headers;
    }
}
