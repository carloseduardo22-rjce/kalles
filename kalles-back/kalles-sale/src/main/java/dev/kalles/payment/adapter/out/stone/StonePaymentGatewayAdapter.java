package dev.kalles.payment.adapter.out.stone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.payment.application.port.out.PaymentGatewayPort;
import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentDocumentPrintCommand;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.kalles.payment.adapter.out.stone.StoneMappingUtils.toAmountInCents;
import static dev.kalles.payment.adapter.out.stone.StoneMappingUtils.toPaymentStatus;
import static dev.kalles.payment.adapter.out.stone.StoneMappingUtils.toProviderCloseStatus;

@Component
public class StonePaymentGatewayAdapter implements PaymentGatewayPort {

    private final StoneCredentialsResolver credentialsResolver;
    private final StoneWebClient stoneWebClient;
    private final ObjectMapper objectMapper;

    public StonePaymentGatewayAdapter(
            StoneCredentialsResolver credentialsResolver,
            StoneWebClient stoneWebClient
    ) {
        this.credentialsResolver = credentialsResolver;
        this.stoneWebClient = stoneWebClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.STONE;
    }

    @Override
    public PaymentResult processPayment(PaymentCommand command) {
        if (command.flow() != dev.kalles.payment.domain.PaymentFlow.TERMINAL) {
            throw new IllegalArgumentException("STONE only supports TERMINAL flow in this integration");
        }

        String stoneFlow = metadataValue(command.metadata(), "stoneFlow", "LIST").toUpperCase();
        validateCommand(command, stoneFlow);

        String payload = toJson(buildCreateOrderPayload(command, stoneFlow));
        ResponseEntity<String> response = stoneWebClient.exchange(
                HttpMethod.POST,
                credentialsResolver.baseUrl() + "/core/v5/orders/",
                payload,
                jsonHeaders()
        );

        if (response.getStatusCode().value() == 409) {
            throw new IllegalStateException("STONE open order limit reached for terminal");
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new StoneAdapterException("Fail to create Stone order. HTTP Status: "
                    + response.getStatusCode().value() + " - " + response.getBody());
        }

        return toPaymentResult(parseJson(response.getBody()));
    }

    @Override
    public PaymentResult getPayment(String providerOrderId) {
        ResponseEntity<String> response = stoneWebClient.exchange(
                HttpMethod.GET,
                credentialsResolver.baseUrl() + "/core/v5/orders/" + providerOrderId,
                null,
                authorizationHeaders()
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new StoneAdapterException("Fail to get Stone order. HTTP Status: "
                    + response.getStatusCode().value() + " - " + response.getBody());
        }

        return toPaymentResult(parseJson(response.getBody()));
    }

    @Override
    public void cancelPayment(String providerOrderId) {
        closePaymentOrder(providerOrderId, PaymentStatus.CANCELED);
    }

    @Override
    public PaymentResult closePaymentOrder(String providerOrderId, PaymentStatus status) {
        String payload = toJson(Map.of("status", toProviderCloseStatus(status)));
        ResponseEntity<String> response = stoneWebClient.exchange(
                HttpMethod.PATCH,
                credentialsResolver.baseUrl() + "/core/v5/orders/" + providerOrderId + "/closed",
                payload,
                jsonHeaders()
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new StoneAdapterException("Fail to close Stone order. HTTP Status: "
                    + response.getStatusCode().value() + " - " + response.getBody());
        }

        return toPaymentResult(parseJson(response.getBody()));
    }

    @Override
    public void printDocument(String providerOrderId, PaymentDocumentPrintCommand command) {
        ResponseEntity<String> response = stoneWebClient.exchange(
                HttpMethod.POST,
                credentialsResolver.baseUrl() + "/posconnect/v1/orders/" + providerOrderId + "/prints",
                toJson(buildPrintPayload(command)),
                jsonHeaders()
        );

        if (response.getStatusCode().value() == 409) {
            throw new IllegalStateException("STONE print request is already in progress for this order");
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new StoneAdapterException("Fail to print Stone document. HTTP Status: "
                    + response.getStatusCode().value() + " - " + response.getBody());
        }
    }

    @Override
    public void refundPayment(String providerPaymentId) {
        throw new IllegalStateException("STONE refunds must be initiated on POS or Stone Portal");
    }

    private void validateCommand(PaymentCommand command, String stoneFlow) {
        if (command.targetId().isBlank()) {
            throw new IllegalArgumentException("targetId is required");
        }

        String customerName = metadataValue(command.metadata(), "customerName", null);
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName is required for STONE");
        }

        String itemDescription = metadataValue(command.metadata(), "itemDescription", null);
        if (itemDescription == null || itemDescription.isBlank()) {
            throw new IllegalArgumentException("itemDescription is required for STONE");
        }

        if (!"DIRECT".equals(stoneFlow) && !"LIST".equals(stoneFlow)) {
            throw new IllegalArgumentException("stoneFlow must be DIRECT or LIST");
        }
    }

    private Map<String, Object> buildCreateOrderPayload(PaymentCommand command, String stoneFlow) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customer", buildCustomerPayload(command));
        payload.put("items", List.of(buildItemPayload(command)));
        payload.put("closed", false);
        payload.put("metadata", new LinkedHashMap<>(Map.of(
                "externalReference", command.externalReference(),
                "idempotencyKey", command.idempotencyKey(),
                "stoneFlow", stoneFlow
        )));
        payload.put("poi_payment_settings", buildPoiPaymentSettings(command, stoneFlow));
        return payload;
    }

    private Map<String, Object> buildCustomerPayload(PaymentCommand command) {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("name", metadataValue(command.metadata(), "customerName", ""));

        String email = metadataValue(command.metadata(), "customerEmail", null);
        if (email != null && !email.isBlank()) {
            customer.put("email", email);
        }

        return customer;
    }

    private Map<String, Object> buildItemPayload(PaymentCommand command) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("amount", toAmountInCents(command.amount()));
        item.put("description", metadataValue(command.metadata(), "itemDescription", command.description()));
        item.put("quantity", integerMetadata(command.metadata(), "quantity", 1));

        String code = metadataValue(command.metadata(), "itemCode", command.externalReference());
        if (code != null && !code.isBlank()) {
            item.put("code", code);
        }

        return item;
    }

    private Map<String, Object> buildPoiPaymentSettings(PaymentCommand command, String stoneFlow) {
        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("visible", booleanMetadata(command.metadata(), "visible", true));
        poi.put("display_name", metadataValue(command.metadata(), "displayName", command.externalReference()));
        poi.put("print_order_receipt", booleanMetadata(command.metadata(), "printOrderReceipt", false));
        poi.put("devices_serial_number", List.of(command.targetId()));

        if ("DIRECT".equals(stoneFlow)) {
            poi.put("payment_setup", buildPaymentSetup(command));
        }

        return poi;
    }

    private Map<String, Object> buildPaymentSetup(PaymentCommand command) {
        Map<String, Object> paymentSetup = new LinkedHashMap<>();

        String paymentType = metadataValue(command.metadata(), "paymentType", toProviderPaymentType(command.methodType()));
        if (paymentType != null && !paymentType.isBlank()) {
            paymentSetup.put("type", paymentType);
        }

        if ("credit".equalsIgnoreCase(paymentType) || "pix".equalsIgnoreCase(paymentType)) {
            paymentSetup.put("installments", integerMetadata(command.metadata(), "installmentCount", 1));
            paymentSetup.put("installment_type", metadataValue(command.metadata(), "installmentType", "merchant"));
        }

        return paymentSetup;
    }

    private Map<String, Object> buildPrintPayload(PaymentDocumentPrintCommand command) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", command.type().name());
        payload.put("size_v", command.sizeVertical());
        payload.put("size_h", command.sizeHorizontal());
        payload.put("format", command.format());
        payload.put("content", command.content());
        return payload;
    }

    private PaymentResult toPaymentResult(JsonNode responseJson) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("code", textOrNull(responseJson.get("code")));
        if (responseJson.has("closed")) {
            metadata.put("closed", responseJson.get("closed").asBoolean());
        }

        JsonNode poiSettings = responseJson.get("poi_payment_settings");
        if (poiSettings != null && poiSettings.has("display_name")) {
            metadata.put("displayName", poiSettings.get("display_name").asText());
        }

        return new PaymentResult(
                textOrNull(responseJson.get("id")),
                extractChargeId(responseJson),
                toPaymentStatus(textOrNull(responseJson.get("status"))),
                metadata
        );
    }

    private String extractChargeId(JsonNode responseJson) {
        JsonNode charges = responseJson.get("charges");
        if (charges != null && charges.isArray() && !charges.isEmpty()) {
            return textOrNull(charges.get(0).get("id"));
        }
        return null;
    }

    private Map<String, String> authorizationHeaders() {
        return Map.of(
                "Authorization", credentialsResolver.authorizationHeader(),
                "ServiceRefererName", credentialsResolver.serviceRefererName(),
                "Accept", "application/json"
        );
    }

    private Map<String, String> jsonHeaders() {
        return Map.of(
                "Authorization", credentialsResolver.authorizationHeader(),
                "ServiceRefererName", credentialsResolver.serviceRefererName(),
                "Accept", "application/json",
                "Content-Type", "application/json"
        );
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (IOException e) {
            throw new StoneAdapterException("Fail to parse Stone response: " + e.getMessage(), e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new StoneAdapterException("Fail to serialize Stone payload: " + e.getMessage(), e);
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private String metadataValue(Map<String, Object> metadata, String key, String fallback) {
        if (metadata == null) {
            return fallback;
        }
        Object value = metadata.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private int integerMetadata(Map<String, Object> metadata, String key, int fallback) {
        if (metadata == null || metadata.get(key) == null) {
            return fallback;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private boolean booleanMetadata(Map<String, Object> metadata, String key, boolean fallback) {
        if (metadata == null || metadata.get(key) == null) {
            return fallback;
        }
        Object value = metadata.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String toProviderPaymentType(PaymentMethodType methodType) {
        return switch (methodType) {
            case CREDIT_CARD -> "credit";
            case DEBIT_CARD -> "debit";
            case VOUCHER_CARD -> "voucher";
            case UNSPECIFIED -> null;
        };
    }
}
