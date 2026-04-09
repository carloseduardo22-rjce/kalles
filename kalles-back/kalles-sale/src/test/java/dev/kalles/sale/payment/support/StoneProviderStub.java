package dev.kalles.sale.payment.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StoneProviderStub {

    private final ObjectMapper objectMapper;
    private final AtomicInteger sequence = new AtomicInteger(1000);
    private final Map<String, StoredOrder> orders = new ConcurrentHashMap<>();

    public StoneProviderStub(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public synchronized void reset() {
        orders.clear();
        sequence.set(1000);
    }

    public synchronized void seedOrder(
            String providerOrderId,
            BigDecimal amount,
            String externalReference,
            String terminalSerialNumber,
            String status,
            boolean closed,
            String stoneFlow
    ) {
        int amountInCents = amount.movePointRight(2).intValueExact();
        orders.put(providerOrderId, new StoredOrder(
                providerOrderId,
                "ST" + sequence.incrementAndGet(),
                amountInCents,
                externalReference,
                terminalSerialNumber,
                status,
                closed,
                stoneFlow,
                null,
                false
        ));
    }

    public synchronized void markPrintInProgress(String providerOrderId) {
        StoredOrder order = requireOrder(providerOrderId);
        orders.put(providerOrderId, order.withPrintInProgress(true));
    }

    public synchronized StoredOrder order(String providerOrderId) {
        return orders.get(providerOrderId);
    }

    public synchronized ResponseEntity<String> handle(
            HttpMethod method,
            String url,
            String body,
            Map<String, String> headers
    ) {
        String path = URI.create(url).getPath();
        if (HttpMethod.POST.equals(method) && "/core/v5/orders/".equals(path)) {
            return createOrder(body);
        }
        if (HttpMethod.GET.equals(method) && path.startsWith("/core/v5/orders/")) {
            return getOrder(path.substring("/core/v5/orders/".length()));
        }
        if (HttpMethod.PATCH.equals(method) && path.startsWith("/core/v5/orders/") && path.endsWith("/closed")) {
            String orderId = path.substring("/core/v5/orders/".length(), path.length() - "/closed".length());
            return closeOrder(orderId, body);
        }
        if (HttpMethod.POST.equals(method) && path.startsWith("/posconnect/v1/orders/") && path.endsWith("/prints")) {
            String orderId = path.substring("/posconnect/v1/orders/".length(), path.length() - "/prints".length());
            return printOrder(orderId);
        }
        return ResponseEntity.status(404).body("{}");
    }

    private ResponseEntity<String> createOrder(String body) {
        JsonNode request = parseJson(body);
        String terminalSerialNumber = request.path("poi_payment_settings").path("devices_serial_number").path(0).asText();
        long openOrders = orders.values().stream()
                .filter(order -> !order.closed())
                .filter(order -> order.terminalSerialNumber().equals(terminalSerialNumber))
                .count();
        if (openOrders >= 30) {
            return ResponseEntity.status(409).body("");
        }

        String providerOrderId = "or_stone_" + sequence.incrementAndGet();
        String externalReference = request.path("metadata").path("externalReference").asText(null);
        String stoneFlow = request.path("metadata").path("stoneFlow").asText("LIST");
        int amount = request.path("items").path(0).path("amount").asInt();
        StoredOrder order = new StoredOrder(
                providerOrderId,
                "ST" + sequence.incrementAndGet(),
                amount,
                externalReference,
                terminalSerialNumber,
                "pending",
                false,
                stoneFlow,
                null,
                false
        );
        orders.put(providerOrderId, order);
        return ResponseEntity.ok(toJson(buildOrderResponse(order, request)));
    }

    private ResponseEntity<String> getOrder(String providerOrderId) {
        StoredOrder order = requireOrder(providerOrderId);
        return ResponseEntity.ok(toJson(buildOrderResponse(order, null)));
    }

    private ResponseEntity<String> closeOrder(String providerOrderId, String body) {
        StoredOrder current = requireOrder(providerOrderId);
        JsonNode request = parseJson(body);
        String status = request.path("status").asText("paid");
        StoredOrder updated = current.withStatus(status).withClosed(true);
        orders.put(providerOrderId, updated);
        return ResponseEntity.ok(toJson(buildOrderResponse(updated, null)));
    }

    private ResponseEntity<String> printOrder(String providerOrderId) {
        StoredOrder current = requireOrder(providerOrderId);
        if (current.printInProgress()) {
            return ResponseEntity.status(409).body("");
        }

        orders.put(providerOrderId, current.withPrintInProgress(true));
        return ResponseEntity.ok().body("");
    }

    private Map<String, Object> buildOrderResponse(StoredOrder order, JsonNode sourceRequest) {
        Instant now = Instant.now();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", order.providerOrderId());
        response.put("code", order.code());
        response.put("amount", order.amountInCents());
        response.put("currency", "BRL");
        response.put("closed", order.closed());
        response.put("status", order.status());
        response.put("created_at", now.toString());
        response.put("updated_at", now.toString());
        if (order.closed()) {
            response.put("closed_at", now.toString());
        }

        response.put("items", List.of(buildItem(sourceRequest, order.amountInCents())));
        response.put("customer", buildCustomer(sourceRequest));
        response.put("metadata", Map.of(
                "externalReference", order.externalReference() == null ? "" : order.externalReference(),
                "stoneFlow", order.stoneFlow()
        ));
        response.put("poi_payment_settings", buildPoiPaymentSettings(sourceRequest, order));

        List<Map<String, Object>> charges = new ArrayList<>();
        if (order.providerPaymentId() != null) {
            charges.add(Map.of("id", order.providerPaymentId()));
        }
        response.put("charges", charges);
        return response;
    }

    private Map<String, Object> buildItem(JsonNode sourceRequest, int amount) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", "oi_" + UUID.randomUUID());
        item.put("type", "product");
        item.put("description", sourceRequest == null ? "Item" : sourceRequest.path("items").path(0).path("description").asText("Item"));
        item.put("amount", amount);
        item.put("quantity", sourceRequest == null ? 1 : sourceRequest.path("items").path(0).path("quantity").asInt(1));
        item.put("status", "active");
        item.put("created_at", Instant.now().toString());
        item.put("updated_at", Instant.now().toString());
        return item;
    }

    private Map<String, Object> buildCustomer(JsonNode sourceRequest) {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", "cus_" + UUID.randomUUID());
        customer.put("name", sourceRequest == null ? "Customer" : sourceRequest.path("customer").path("name").asText("Customer"));
        if (sourceRequest != null && sourceRequest.path("customer").has("email")) {
            customer.put("email", sourceRequest.path("customer").path("email").asText());
        }
        customer.put("delinquent", false);
        customer.put("created_at", Instant.now().toString());
        customer.put("updated_at", Instant.now().toString());
        customer.put("phones", Map.of());
        return customer;
    }

    private Map<String, Object> buildPoiPaymentSettings(JsonNode sourceRequest, StoredOrder order) {
        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("visible", !order.closed());
        poi.put("display_name", sourceRequest == null
                ? order.externalReference()
                : sourceRequest.path("poi_payment_settings").path("display_name").asText(order.externalReference()));
        poi.put("print_order_receipt", sourceRequest != null && sourceRequest.path("poi_payment_settings").path("print_order_receipt").asBoolean(false));
        poi.put("devices_serial_number", List.of(order.terminalSerialNumber()));
        if (sourceRequest != null && sourceRequest.path("poi_payment_settings").has("payment_setup")) {
            Map<String, Object> paymentSetup = new LinkedHashMap<>();
            JsonNode sourceSetup = sourceRequest.path("poi_payment_settings").path("payment_setup");
            if (sourceSetup.has("type")) {
                paymentSetup.put("type", sourceSetup.path("type").asText());
            }
            if (sourceSetup.has("installments")) {
                paymentSetup.put("installments", sourceSetup.path("installments").asInt());
            }
            if (sourceSetup.has("installment_type")) {
                paymentSetup.put("installment_type", sourceSetup.path("installment_type").asText());
            }
            poi.put("payment_setup", paymentSetup);
        }
        poi.put("updated_at", Instant.now().toString());
        poi.put("created_at", Instant.now().toString());
        return poi;
    }

    private StoredOrder requireOrder(String providerOrderId) {
        StoredOrder order = orders.get(providerOrderId);
        if (order == null) {
            throw new IllegalArgumentException("Stone stub order not found: " + providerOrderId);
        }
        return order;
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse stub body", e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize stub response", e);
        }
    }

    public record StoredOrder(
            String providerOrderId,
            String code,
            int amountInCents,
            String externalReference,
            String terminalSerialNumber,
            String status,
            boolean closed,
            String stoneFlow,
            String providerPaymentId,
            boolean printInProgress
    ) {
        public StoredOrder withStatus(String newStatus) {
            return new StoredOrder(
                    providerOrderId,
                    code,
                    amountInCents,
                    externalReference,
                    terminalSerialNumber,
                    newStatus,
                    closed,
                    stoneFlow,
                    providerPaymentId,
                    printInProgress
            );
        }

        public StoredOrder withClosed(boolean newClosed) {
            return new StoredOrder(
                    providerOrderId,
                    code,
                    amountInCents,
                    externalReference,
                    terminalSerialNumber,
                    status,
                    newClosed,
                    stoneFlow,
                    providerPaymentId,
                    printInProgress
            );
        }

        public StoredOrder withPrintInProgress(boolean newPrintInProgress) {
            return new StoredOrder(
                    providerOrderId,
                    code,
                    amountInCents,
                    externalReference,
                    terminalSerialNumber,
                    status,
                    closed,
                    stoneFlow,
                    providerPaymentId,
                    newPrintInProgress
            );
        }
    }
}
