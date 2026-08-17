package dev.kalles.payment.adapter.out.mercadopago;

import dev.kalles.cashregister.entity.CashRegister;
import dev.kalles.cashregister.repository.CashRegisterRepository;
import dev.kalles.payment.application.port.out.PaymentPointRepository;
import dev.kalles.payment.domain.PaymentCommand;
import dev.kalles.payment.domain.PaymentFlow;
import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentPoint;
import dev.kalles.payment.domain.PaymentProvider;
import dev.kalles.payment.domain.PaymentResult;
import dev.kalles.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoPaymentGatewayAdapterTest {

    private static final String ORDERS_URL = "https://api.mercadopago.com/v1/orders";
    private static final String ORDER_ID = "ORD-01JQ";

    private MockRestServiceServer server;
    private CashRegisterRepository cashRegisterRepository;
    private PaymentPointRepository paymentPointRepository;
    private MercadoPagoPaymentGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        MercadoPagoCredentialsResolver credentialsResolver = mock(MercadoPagoCredentialsResolver.class);
        when(credentialsResolver.fallbackAccessToken()).thenReturn("token-do-tenant");

        cashRegisterRepository = mock(CashRegisterRepository.class);
        paymentPointRepository = mock(PaymentPointRepository.class);

        adapter = new MercadoPagoPaymentGatewayAdapter(
                credentialsResolver,
                new MercadoPagoWebClient(builder),
                cashRegisterRepository,
                paymentPointRepository,
                JsonMapper.builder().build()
        );
    }

    private PaymentCommand command(PaymentFlow flow, String targetId) {
        return new PaymentCommand(
                PaymentProvider.MERCADO_PAGO,
                flow,
                "VENDA-2026-001",
                new BigDecimal("150.90"),
                targetId,
                "chave-idempotente-01",
                "Venda 2026-001",
                PaymentMethodType.DEBIT_CARD,
                Map.of()
        );
    }

    private void seedCashRegisterWithPoint() {
        UUID cashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = mock(CashRegister.class);
        when(cashRegister.getId()).thenReturn(cashRegisterId);

        when(cashRegisterRepository.findByCode("CAIXA-01")).thenReturn(Optional.of(cashRegister));
        when(paymentPointRepository.findByCashRegisterIdAndProvider(eq(cashRegisterId), any()))
                .thenReturn(Optional.of(new PaymentPoint(
                        UUID.randomUUID(),
                        cashRegisterId,
                        PaymentProvider.MERCADO_PAGO,
                        "CAIXA-01",
                        "777000111"
                )));
    }

    @Test
    void shouldCreateTheQrOrderAndReturnTheQrData() {
        seedCashRegisterWithPoint();
        server.expect(requestTo(ORDERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andExpect(header("X-Idempotency-Key", "chave-idempotente-01"))
                .andExpect(header("Content-Type", "application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.type").value("qr"))
                .andExpect(jsonPath("$.total_amount").value("150.90"))
                .andExpect(jsonPath("$.external_reference").value("VENDA-2026-001"))
                .andExpect(jsonPath("$.config.qr.external_pos_id").value("CAIXA-01"))
                .andExpect(jsonPath("$.config.qr.mode").value("dynamic"))
                .andExpect(jsonPath("$.transactions.payments[0].amount").value("150.90"))
                .andExpect(content().string(not(containsString("description"))))
                .andRespond(withSuccess("""
                        {
                          "id": "ORD-01JQ",
                          "status": "created",
                          "type_response": {"qr_data": "00020101021243650016COM.MERCADOLIBRE"}
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = adapter.processPayment(command(PaymentFlow.QR_CODE, "CAIXA-01"));

        assertThat(result.providerOrderId()).isEqualTo(ORDER_ID);
        assertThat(result.providerPaymentId()).isNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.metadata()).containsEntry("qrData", "00020101021243650016COM.MERCADOLIBRE");
        server.verify();
    }

    @Test
    void shouldFailTheQrOrderWhenMercadoPagoOmitsTheQrData() {
        seedCashRegisterWithPoint();
        server.expect(requestTo(ORDERS_URL))
                .andRespond(withSuccess("""
                        {"id": "ORD-01JQ", "status": "created", "type_response": {}}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.processPayment(command(PaymentFlow.QR_CODE, "CAIXA-01")))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("qr_data");
        server.verify();
    }

    @Test
    void shouldFailTheQrOrderWhenTheCashRegisterHasNoPoint() {
        UUID cashRegisterId = UUID.randomUUID();
        CashRegister cashRegister = mock(CashRegister.class);
        when(cashRegister.getId()).thenReturn(cashRegisterId);
        when(cashRegisterRepository.findByCode("CAIXA-01")).thenReturn(Optional.of(cashRegister));
        when(paymentPointRepository.findByCashRegisterIdAndProvider(eq(cashRegisterId), any()))
                .thenReturn(Optional.of(new PaymentPoint(
                        UUID.randomUUID(), cashRegisterId, PaymentProvider.MERCADO_PAGO, "CAIXA-01", null)));

        assertThatThrownBy(() -> adapter.processPayment(command(PaymentFlow.QR_CODE, "CAIXA-01")))
                .isInstanceOf(IllegalStateException.class);
        server.verify();
    }

    @Test
    void shouldCreateThePointOrderAndReadTheFirstPaymentId() {
        server.expect(requestTo(ORDERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Idempotency-Key", "chave-idempotente-01"))
                .andExpect(jsonPath("$.type").value("point"))
                .andExpect(jsonPath("$.external_reference").value("VENDA-2026-001"))
                .andExpect(jsonPath("$.description").value("Venda 2026-001"))
                .andExpect(jsonPath("$.config.point.terminal_id").value("TERMINAL-01"))
                .andExpect(jsonPath("$.config.point.print_on_terminal").value("no_ticket"))
                .andExpect(jsonPath("$.config.payment_method.default_type").value("debit_card"))
                .andExpect(jsonPath("$.transactions.payments[0].amount").value("150.90"))
                .andExpect(content().string(not(containsString("total_amount"))))
                .andRespond(withSuccess("""
                        {
                          "id": "ORD-01JQ",
                          "status": "at_terminal",
                          "transactions": {"payments": [{"id": "PAY-9001"}, {"id": "PAY-9002"}]}
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = adapter.processPayment(command(PaymentFlow.TERMINAL, "TERMINAL-01"));

        assertThat(result.providerOrderId()).isEqualTo(ORDER_ID);
        assertThat(result.providerPaymentId()).isEqualTo("PAY-9001");
        assertThat(result.status()).isEqualTo(PaymentStatus.IN_PROGRESS);
        assertThat(result.metadata()).isEmpty();
        server.verify();
    }

    @Test
    void shouldFailThePointOrderWhenMercadoPagoRejectsIt() {
        server.expect(requestTo(ORDERS_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid terminal\"}"));

        assertThatThrownBy(() -> adapter.processPayment(command(PaymentFlow.TERMINAL, "TERMINAL-01")))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("400");
        server.verify();
    }

    @Test
    void shouldGetTheOrderWithoutAPaymentYet() {
        server.expect(requestTo(ORDERS_URL + "/" + ORDER_ID))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token-do-tenant"))
                .andRespond(withSuccess("""
                        {"id": "ORD-01JQ", "status": "processed", "transactions": {"payments": []}}
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = adapter.getPayment(ORDER_ID);

        assertThat(result.providerOrderId()).isEqualTo(ORDER_ID);
        assertThat(result.providerPaymentId()).isNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.APPROVED);
        server.verify();
    }

    @Test
    void shouldFallBackToUnknownWhenTheOrderHasNoStatus() {
        server.expect(requestTo(ORDERS_URL + "/" + ORDER_ID))
                .andRespond(withSuccess("{\"id\": \"ORD-01JQ\"}", MediaType.APPLICATION_JSON));

        assertThat(adapter.getPayment(ORDER_ID).status()).isEqualTo(PaymentStatus.UNKNOWN);
        server.verify();
    }

    @Test
    void shouldFailToGetAnOrderMercadoPagoDoesNotKnow() {
        server.expect(requestTo(ORDERS_URL + "/" + ORDER_ID))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> adapter.getPayment(ORDER_ID))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("404");
        server.verify();
    }

    @Test
    void shouldCancelTheOrder() {
        server.expect(requestTo(ORDERS_URL + "/" + ORDER_ID + "/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/json; charset=UTF-8"))
                .andRespond(withSuccess("{\"id\": \"ORD-01JQ\", \"status\": \"canceled\"}", MediaType.APPLICATION_JSON));

        adapter.cancelPayment(ORDER_ID);

        server.verify();
    }

    @Test
    void shouldCloseTheOrderByCancelingAndReadingItBack() {
        server.expect(requestTo(ORDERS_URL + "/" + ORDER_ID + "/cancel"))
                .andRespond(withSuccess("{\"id\": \"ORD-01JQ\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ORDERS_URL + "/" + ORDER_ID))
                .andRespond(withSuccess("{\"id\": \"ORD-01JQ\", \"status\": \"canceled\"}", MediaType.APPLICATION_JSON));

        PaymentResult result = adapter.closePaymentOrder(ORDER_ID, PaymentStatus.CANCELED);

        assertThat(result.status()).isEqualTo(PaymentStatus.CANCELED);
        server.verify();
    }

    @Test
    void shouldRefuseToCloseTheOrderWithAnyStatusOtherThanCanceled() {
        assertThatThrownBy(() -> adapter.closePaymentOrder(ORDER_ID, PaymentStatus.APPROVED))
                .isInstanceOf(IllegalArgumentException.class);
        server.verify();
    }

    @Test
    void shouldRefundThePayment() {
        server.expect(requestTo("https://api.mercadopago.com/v1/payments/PAY-9001/refunds"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\": \"REF-01\"}", MediaType.APPLICATION_JSON));

        adapter.refundPayment("PAY-9001");

        server.verify();
    }

    @Test
    void shouldFailWhenMercadoPagoRejectsTheRefund() {
        server.expect(requestTo("https://api.mercadopago.com/v1/payments/PAY-9001/refunds"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> adapter.refundPayment("PAY-9001"))
                .isInstanceOf(MercadoPagoAdapterException.class)
                .hasMessageContaining("409");
        server.verify();
    }
}
