package dev.kalles.sale.payment.steps;

import dev.kalles.sale.payment.adapter.out.stone.persistence.repository.StonePaymentOrderJpaRepository;
import dev.kalles.sale.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentOrder;
import dev.kalles.sale.payment.domain.PaymentProvider;
import dev.kalles.sale.payment.domain.PaymentStatus;
import dev.kalles.sale.payment.support.LocalHttpTestClient;
import dev.kalles.sale.payment.support.StoneProviderStub;
import dev.kalles.sale.payment.support.StoneWebhookEventProbe;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StonePaymentSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private StoneProviderStub stoneProviderStub;

    @Autowired
    private StoneWebhookEventProbe webhookEventProbe;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private StonePaymentOrderJpaRepository stonePaymentOrderJpaRepository;

    private final Map<String, Object> paymentRequest = new LinkedHashMap<>();
    private final Map<String, Object> webhookPayload = new LinkedHashMap<>();
    private final Map<String, Object> webhookData = new LinkedHashMap<>();
    private final Map<String, Object> webhookOrder = new LinkedHashMap<>();
    private final Map<String, Object> webhookOrderMetadata = new LinkedHashMap<>();
    private final Map<String, Object> webhookProviderMetadata = new LinkedHashMap<>();
    private final Map<String, Object> printRequest = new LinkedHashMap<>();

    private Response response;
    private String currentProviderOrderId;

    @Before("@stone")
    public void setUpScenario() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        stoneProviderStub.reset();
        webhookEventProbe.reset();
        stonePaymentOrderJpaRepository.deleteAll();
        paymentRequest.clear();
        webhookPayload.clear();
        webhookData.clear();
        webhookOrder.clear();
        webhookOrderMetadata.clear();
        webhookProviderMetadata.clear();
        printRequest.clear();
        response = null;
        currentProviderOrderId = null;
    }

    @Dado("que o provider de pagamento {string} esta habilitado no contexto de payment")
    public void givenProviderIsEnabled(String provider) {
        assertThat(provider).isEqualTo("STONE");
    }

    @Dado("uma solicitacao generica de pagamento com provider {string}")
    public void givenGenericPaymentRequest(String provider) {
        paymentRequest.clear();
        paymentRequest.put("provider", provider);
        paymentRequest.put("metadata", new LinkedHashMap<String, Object>());
    }

    @Dado("o flow informado e {string}")
    public void givenFlow(String flow) {
        paymentRequest.put("flow", flow);
    }

    @Dado("o externalReference informado e {string}")
    public void givenExternalReference(String externalReference) {
        paymentRequest.put("externalReference", externalReference);
    }

    @Dado("o amount informado e {string}")
    public void givenAmount(String amount) {
        paymentRequest.put("amount", amount);
    }

    @Dado("o targetId informado e {string}")
    public void givenTargetId(String targetId) {
        paymentRequest.put("targetId", targetId);
    }

    @Dado("o methodType informado e {string}")
    public void givenMethodType(String methodType) {
        paymentRequest.put("methodType", methodType);
    }

    @Dado("a metadata contem:")
    public void givenMetadata(DataTable dataTable) {
        Map<String, Object> metadata = metadata();
        dataTable.asMaps().forEach(row -> metadata.put(row.get("key"), row.get("value")));
    }

    @Quando("o cliente processar o pagamento no endpoint generico de pagamentos")
    public void whenClientProcessesPayment() {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .when()
                .post("/api/payments/process");

        currentProviderOrderId = response.jsonPath().getString("providerOrderId");
    }

    @Entao("a resposta deve ter status HTTP {int}")
    public void thenResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("a resposta deve conter o provider {string}")
    public void thenResponseContainsProvider(String provider) {
        assertThat(response.jsonPath().getString("provider")).isEqualTo(provider);
    }

    @Entao("a resposta deve conter um providerOrderId preenchido")
    public void thenResponseContainsFilledProviderOrderId() {
        assertThat(response.jsonPath().getString("providerOrderId")).isNotBlank();
    }

    @Entao("a resposta deve conter status {string}")
    public void thenResponseContainsStatus(String status) {
        assertThat(response.jsonPath().getString("status")).isEqualTo(status);
    }

    @Entao("a resposta deve conter a mensagem {string}")
    public void thenResponseContainsMessage(String message) {
        assertThat(response.jsonPath().getString("detail")).contains(message);
    }

    @Dado("que existe um pagamento Stone previamente criado com providerOrderId {string}")
    public void givenExistingStonePayment(String providerOrderId) {
        currentProviderOrderId = providerOrderId;
        seedOrder(providerOrderId, new BigDecimal("89.90"), "ERP-RESILIENCE-1", "6N021236", "pending", false, "LIST");
    }

    @Quando("o cliente consultar o endpoint generico de pagamentos com provider {string} e providerOrderId {string}")
    public void whenClientGetsPayment(String provider, String providerOrderId) {
        response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/payments/" + provider + "/" + providerOrderId,
                Map.of("Accept", "application/json")
        );
    }

    @Entao("a resposta deve conter o providerOrderId {string}")
    public void thenResponseContainsProviderOrderId(String providerOrderId) {
        assertThat(response.jsonPath().getString("providerOrderId")).isEqualTo(providerOrderId);
    }

    @Entao("a resposta deve conter um status de pagamento valido do dominio")
    public void thenResponseContainsValidDomainStatus() {
        String status = response.jsonPath().getString("status");
        assertThat(List.of(PaymentStatus.values()).stream().map(Enum::name).toList()).contains(status);
    }

    @Dado("que existe um pedido Stone aberto com providerOrderId {string}")
    public void givenExistingOpenOrder(String providerOrderId) {
        currentProviderOrderId = providerOrderId;
        seedOrder(providerOrderId, new BigDecimal("125.00"), "ERP-SALE-" + providerOrderId, "6N021234", "pending", false, "DIRECT");
    }

    @Dado("o pagamento do pedido foi confirmado por webhook")
    public void givenOrderWasConfirmedByWebhook() {
        assertThat(currentProviderOrderId).isNotBlank();
    }

    @Quando("o sistema solicitar o fechamento do pedido Stone com status final {string}")
    public void whenSystemClosesStoneOrder(String finalStatus) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("status", toDomainStatus(finalStatus).name()))
                .when()
                .post("/api/payments/STONE/{providerOrderId}/close", currentProviderOrderId);
    }

    @Entao("a resposta de fechamento deve ter status HTTP {int}")
    public void thenCloseResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o pedido Stone deve ficar invisivel para o POS")
    public void thenStoneOrderShouldBeInvisibleToPos() {
        assertThat(stoneProviderStub.order(currentProviderOrderId)).isNotNull();
        assertThat(stoneProviderStub.order(currentProviderOrderId).closed()).isTrue();
    }

    @Quando("o cliente solicitar o cancelamento generico do pagamento com provider {string} e providerOrderId {string}")
    public void whenClientCancelsPayment(String provider, String providerOrderId) {
        currentProviderOrderId = providerOrderId;
        response = RestAssured.given()
                .when()
                .post("/api/payments/{provider}/{providerOrderId}/cancel", provider, providerOrderId);
    }

    @Entao("a resposta de cancelamento deve ter status HTTP {int}")
    public void thenCancelResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o pedido Stone deve ser fechado com status final {string}")
    public void thenStoneOrderShouldHaveFinalStatus(String finalStatus) {
        assertThat(stoneProviderStub.order(currentProviderOrderId)).isNotNull();
        assertThat(stoneProviderStub.order(currentProviderOrderId).status()).isEqualTo(finalStatus);
        assertThat(stoneProviderStub.order(currentProviderOrderId).closed()).isTrue();
    }

    @Dado("que existe um pedido Stone pago com providerOrderId {string}")
    public void givenExistingPaidOrder(String providerOrderId) {
        currentProviderOrderId = providerOrderId;
        seedOrder(providerOrderId, new BigDecimal("90.00"), "ERP-SALE-" + providerOrderId, "6N021240", "paid", true, "DIRECT");
    }

    @Dado("existe um documento fiscal para impressao com:")
    public void givenFiscalDocument(DataTable dataTable) {
        printRequest.clear();
        dataTable.asMaps().forEach(row -> printRequest.put(row.get("key"), row.get("value")));
    }

    @Dado("ja existe uma requisicao de impressao em processamento para esse pedido")
    public void givenPrintAlreadyInProgress() {
        stoneProviderStub.markPrintInProgress(currentProviderOrderId);
    }

    @Quando("o sistema solicitar a impressao do documento no terminal Stone")
    public void whenSystemRequestsPrint() {
        String type = String.valueOf(printRequest.getOrDefault("type", "NFE"));
        int sizeVertical = Integer.parseInt(String.valueOf(printRequest.getOrDefault("size_v", "128")));
        int sizeHorizontal = Integer.parseInt(String.valueOf(printRequest.getOrDefault("size_h", "384")));
        String format = String.valueOf(printRequest.getOrDefault("format", "png"));
        String content = String.valueOf(printRequest.getOrDefault("content", "A1b2cDefghiJkWlMn9PQrStUVABCDEF=="));

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", type,
                        "sizeVertical", sizeVertical,
                        "sizeHorizontal", sizeHorizontal,
                        "format", format,
                        "content", content
                ))
                .when()
                .post("/api/payments/STONE/{providerOrderId}/documents/print", currentProviderOrderId);
    }

    @Entao("a resposta de impressao deve ter status HTTP {int}")
    public void thenPrintResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Dado("um webhook Stone do tipo {string}")
    public void givenStoneWebhookOfType(String type) {
        webhookPayload.clear();
        webhookData.clear();
        webhookOrder.clear();
        webhookOrderMetadata.clear();
        webhookProviderMetadata.clear();
        webhookPayload.put("type", type);
        webhookData.put("metadata", webhookProviderMetadata);
        webhookData.put("order", webhookOrder);
        webhookOrder.put("metadata", webhookOrderMetadata);
        if ("charge.paid".equals(type)) {
            webhookData.put("status", "paid");
        } else if ("charge.refunded".equals(type)) {
            webhookData.put("status", "canceled");
        }
        webhookPayload.put("data", webhookData);
    }

    @Dado("o webhook referencia o providerOrderId {string}")
    public void givenWebhookReferencesProviderOrderId(String providerOrderId) {
        currentProviderOrderId = providerOrderId;
        webhookOrder.put("id", providerOrderId);
    }

    @Dado("o webhook referencia o providerPaymentId {string}")
    public void givenWebhookReferencesProviderPaymentId(String providerPaymentId) {
        webhookData.put("id", providerPaymentId);
    }

    @Dado("o webhook informa status externo {string}")
    public void givenWebhookExternalStatus(String status) {
        webhookData.put("status", status);
    }

    @Dado("o webhook informa paymentMethod externo {string}")
    public void givenWebhookExternalPaymentMethod(String paymentMethod) {
        webhookData.put("payment_method", paymentMethod);
    }

    @Dado("o webhook informa terminalSerialNumber {string}")
    public void givenWebhookTerminalSerialNumber(String terminalSerialNumber) {
        webhookProviderMetadata.put("terminal_serial_number", terminalSerialNumber);
    }

    @Dado("o webhook informa paidAmount externo {string}")
    public void givenWebhookPaidAmount(String amount) {
        webhookData.put("paid_amount", cents(amount));
    }

    @Dado("o pedido original Stone foi criado com amount {string} no fluxo {string}")
    public void givenOriginalStoneOrderAmount(String amount, String flow) {
        webhookOrder.put("amount", cents(amount));
        webhookOrderMetadata.put("stoneFlow", flow);
        webhookOrderMetadata.put("externalReference", "ERP-LIST-1");
        paymentOrderRepository.save(new PaymentOrder(
                PaymentProvider.STONE,
                currentProviderOrderId,
                null,
                PaymentStatus.PENDING,
                "ERP-LIST-1",
                new BigDecimal(amount),
                "idem-list-1",
                PaymentFlow.TERMINAL,
                PaymentMethodType.UNSPECIFIED
        ));
    }

    @Dado("o pedido do webhook nao existe previamente no ERP")
    public void givenWebhookOrderDoesNotExistInErp() {
        assertThat(paymentOrderRepository.findByProviderOrderIdAndProvider(currentProviderOrderId, PaymentProvider.STONE)).isEmpty();
    }

    @Quando("o provider enviar o callback para o webhook especifico da Stone")
    public void whenProviderSendsStoneWebhook() {
        if (!webhookData.containsKey("paid_amount") && webhookData.containsKey("amount")) {
            webhookData.put("paid_amount", webhookData.get("amount"));
        }
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(webhookPayload)
                .when()
                .post("/api/webhooks/stone");
    }

    @Entao("a resposta do webhook deve ter status HTTP {int}")
    public void thenWebhookResponseHasStatus(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @Entao("o evento deve ser traduzido para o provider {string}")
    public void thenEventShouldBeTranslatedToProvider(String provider) {
        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(webhookEventProbe.lastEvent().provider().name()).isEqualTo(provider);
    }

    @Entao("o evento deve ser traduzido para o status de pagamento {string}")
    public void thenEventShouldBeTranslatedToPaymentStatus(String status) {
        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(webhookEventProbe.lastEvent().status()).isEqualTo(PaymentStatus.valueOf(status));
    }

    @Entao("o evento deve preservar o providerOrderId {string}")
    public void thenEventShouldPreserveProviderOrderId(String providerOrderId) {
        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(webhookEventProbe.lastEvent().providerOrderId()).isEqualTo(providerOrderId);
    }

    @Entao("o evento deve preservar o providerPaymentId {string}")
    public void thenEventShouldPreserveProviderPaymentId(String providerPaymentId) {
        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(webhookEventProbe.lastEvent().providerPaymentId()).isEqualTo(providerPaymentId);
    }

    @Entao("o evento deve registrar na metadata que o valor pago divergiu do valor original")
    public void thenEventShouldRegisterAmountDivergence() {
        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(webhookEventProbe.lastEvent().metadata()).containsEntry("amountDivergence", true);
    }

    @Entao("o sistema deve aceitar o evento sem exigir pedido previamente persistido")
    public void thenSystemShouldAcceptAvulsaEvent() {
        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(paymentOrderRepository.findByProviderOrderIdAndProvider(currentProviderOrderId, PaymentProvider.STONE)).isEmpty();
    }

    @Dado("que ja existem 30 pedidos Stone abertos para o terminal {string}")
    public void givenThereAreThirtyOpenStoneOrders(String terminalSerialNumber) {
        for (int index = 0; index < 30; index++) {
            stoneProviderStub.seedOrder(
                    "or_stone_open_" + index,
                    new BigDecimal("10.00"),
                    "ERP-OPEN-" + index,
                    terminalSerialNumber,
                    "pending",
                    false,
                    "LIST"
            );
        }
    }

    @Entao("o sistema nao deve alterar nenhum pagamento")
    public void thenSystemShouldNotChangeAnyPayment() {
        assertThat(webhookEventProbe.eventCount()).isZero();
    }

    private Map<String, Object> metadata() {
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) paymentRequest.get("metadata");
        return metadata;
    }

    private void seedOrder(
            String providerOrderId,
            BigDecimal amount,
            String externalReference,
            String terminalSerialNumber,
            String providerStatus,
            boolean closed,
            String stoneFlow
    ) {
        stoneProviderStub.seedOrder(providerOrderId, amount, externalReference, terminalSerialNumber, providerStatus, closed, stoneFlow);
        paymentOrderRepository.save(new PaymentOrder(
                PaymentProvider.STONE,
                providerOrderId,
                null,
                switch (providerStatus) {
                    case "paid" -> PaymentStatus.APPROVED;
                    case "canceled" -> PaymentStatus.CANCELED;
                    default -> PaymentStatus.PENDING;
                },
                externalReference,
                amount,
                "idem-" + providerOrderId,
                PaymentFlow.TERMINAL,
                PaymentMethodType.CREDIT_CARD
        ));
    }

    private PaymentStatus toDomainStatus(String providerStatus) {
        return switch (providerStatus.toLowerCase()) {
            case "paid" -> PaymentStatus.APPROVED;
            case "canceled", "cancelled" -> PaymentStatus.CANCELED;
            case "failed" -> PaymentStatus.FAILED;
            default -> throw new IllegalArgumentException("Unsupported Stone close status in feature: " + providerStatus);
        };
    }

    private int cents(String amount) {
        return new BigDecimal(amount).movePointRight(2).intValueExact();
    }
}
