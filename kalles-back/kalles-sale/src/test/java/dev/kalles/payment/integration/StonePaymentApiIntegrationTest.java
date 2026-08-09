package dev.kalles.payment.integration;

import dev.kalles.KallesSaleApplication;
import dev.kalles.payment.application.port.out.PaymentOrderRepository;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentOrder;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentStatus;
import dev.kalles.payment.support.AbstractStonePaymentContainerSupport;
import dev.kalles.payment.support.LocalHttpTestClient;
import dev.kalles.payment.support.StonePaymentTestConfiguration;
import dev.kalles.payment.support.StoneProviderStub;
import dev.kalles.payment.support.StoneWebhookEventProbe;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KallesSaleApplication.class)
@Import(StonePaymentTestConfiguration.class)
class StonePaymentApiIntegrationTest extends AbstractStonePaymentContainerSupport {

    @LocalServerPort
    private int port;

    @Autowired
    private StoneProviderStub stoneProviderStub;

    @Autowired
    private StoneWebhookEventProbe webhookEventProbe;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @BeforeEach
    void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        stoneProviderStub.reset();
        webhookEventProbe.reset();
    }

    @Test
    void shouldProcessStoneDirectPaymentThroughGenericEndpoint() {
        String providerOrderId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "provider", "STONE",
                        "flow", "TERMINAL",
                        "externalReference", "ERP-SALE-1001",
                        "amount", "125.00",
                        "targetId", "6N021234",
                        "methodType", "CREDIT_CARD",
                        "metadata", Map.of(
                                "stoneFlow", "DIRECT",
                                "customerName", "Tony Stark",
                                "customerEmail", "tony@kalles.com",
                                "itemDescription", "Venda PDV 1001",
                                "itemCode", "SKU-1001",
                                "displayName", "Pedido #1001",
                                "visible", true,
                                "printOrderReceipt", false,
                                "installmentCount", 1,
                                "installmentType", "merchant"
                        )
                ))
                .when()
                .post("/api/payments/process")
                .then()
                .statusCode(200)
                .body("provider", equalTo("STONE"))
                .body("status", equalTo("PENDING"))
                .extract()
                .path("providerOrderId");

        assertThat(providerOrderId).isNotBlank();
        assertThat(paymentOrderRepository.findByProviderOrderIdAndProvider(providerOrderId, PaymentProvider.STONE))
                .isPresent()
                .get()
                .extracting(PaymentOrder::status)
                .isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldCloseStoneOrderThroughGenericEndpoint() {
        stoneProviderStub.seedOrder(
                "or_stone_paid_1",
                new BigDecimal("125.00"),
                "ERP-SALE-1001",
                "6N021234",
                "pending",
                false,
                "DIRECT"
        );
        paymentOrderRepository.save(new PaymentOrder(
                PaymentProvider.STONE,
                "or_stone_paid_1",
                null,
                PaymentStatus.PENDING,
                "ERP-SALE-1001",
                new BigDecimal("125.00"),
                "idem-close-1",
                PaymentFlow.TERMINAL,
                PaymentMethodType.CREDIT_CARD
        ));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("status", "APPROVED"))
                .when()
                .post("/api/payments/STONE/or_stone_paid_1/close")
                .then()
                .statusCode(200);

        assertThat(stoneProviderStub.order("or_stone_paid_1")).isNotNull();
        assertThat(stoneProviderStub.order("or_stone_paid_1").closed()).isTrue();
        assertThat(paymentOrderRepository.findByProviderOrderIdAndProvider("or_stone_paid_1", PaymentProvider.STONE))
                .isPresent()
                .get()
                .extracting(PaymentOrder::status)
                .isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void shouldGetStoneOrderThroughGenericEndpoint() {
        stoneProviderStub.seedOrder(
                "or_stone_123",
                new BigDecimal("89.90"),
                "ERP-RESILIENCE-1",
                "6N021236",
                "pending",
                false,
                "LIST"
        );
        paymentOrderRepository.save(new PaymentOrder(
                PaymentProvider.STONE,
                "or_stone_123",
                null,
                PaymentStatus.PENDING,
                "ERP-RESILIENCE-1",
                new BigDecimal("89.90"),
                "idem-resilience-1",
                PaymentFlow.TERMINAL,
                PaymentMethodType.CREDIT_CARD
        ));
        Response response = LocalHttpTestClient.get(
                "http://localhost:" + port + "/api/payments/STONE/or_stone_123",
                Map.of("Accept", "application/json")
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("provider")).isEqualTo("STONE");
        assertThat(response.jsonPath().getString("providerOrderId")).isEqualTo("or_stone_123");
        assertThat(response.jsonPath().getString("status")).isEqualTo("PENDING");
    }

    @Test
    void shouldReturnConflictWhenPrintIsAlreadyInProgress() {
        stoneProviderStub.seedOrder(
                "or_stone_print_2",
                new BigDecimal("90.00"),
                "ERP-SALE-PRINT-2",
                "6N021236",
                "paid",
                true,
                "DIRECT"
        );
        stoneProviderStub.markPrintInProgress("or_stone_print_2");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "NFE",
                        "sizeVertical", 128,
                        "sizeHorizontal", 384,
                        "format", "png",
                        "content", "A1b2cDefghiJkWlMn9PQrStUVABCDEF=="
                ))
                .when()
                .post("/api/payments/STONE/or_stone_print_2/documents/print")
                .then()
                .statusCode(409)
                .body("detail", equalTo("STONE print request is already in progress for this order"));
    }

    @Test
    void shouldTranslateStoneWebhookIntoDomainEvent() {
        paymentOrderRepository.save(new PaymentOrder(
                PaymentProvider.STONE,
                "or_stone_123",
                null,
                PaymentStatus.PENDING,
                "ERP-SALE-1001",
                new BigDecimal("125.00"),
                "idem-webhook-1",
                PaymentFlow.TERMINAL,
                PaymentMethodType.CREDIT_CARD
        ));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "charge.paid",
                        "data", Map.of(
                                "id", "ch_stone_987",
                                "amount", 12500,
                                "paid_amount", 12500,
                                "status", "paid",
                                "payment_method", "credit",
                                "order", Map.of(
                                        "id", "or_stone_123",
                                        "amount", 12500,
                                        "status", "pending",
                                        "metadata", Map.of("externalReference", "ERP-SALE-1001")
                                ),
                                "metadata", Map.of(
                                        "account_funding_source", "Credit",
                                        "terminal_serial_number", "6N021234"
                                )
                        )
                ))
                .when()
                .post("/api/webhooks/stone")
                .then()
                .statusCode(200);

        assertThat(webhookEventProbe.lastEvent()).isNotNull();
        assertThat(webhookEventProbe.lastEvent().status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(webhookEventProbe.lastEvent().providerOrderId()).isEqualTo("or_stone_123");
        assertThat(webhookEventProbe.lastEvent().providerPaymentId()).isEqualTo("ch_stone_987");
    }

    @Test
    void shouldReturnAcceptedForUnsupportedStoneWebhook() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("type", "charge.unknown", "data", Map.of()))
                .when()
                .post("/api/webhooks/stone")
                .then()
                .statusCode(202);

        assertThat(webhookEventProbe.lastEvent()).isNull();
    }
}
