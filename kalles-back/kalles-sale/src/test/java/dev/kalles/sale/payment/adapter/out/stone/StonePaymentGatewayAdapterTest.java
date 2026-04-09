package dev.kalles.sale.payment.adapter.out.stone;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kalles.sale.payment.domain.PaymentCommand;
import dev.kalles.sale.payment.domain.PaymentFlow;
import dev.kalles.sale.payment.domain.PaymentMethodType;
import dev.kalles.sale.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StonePaymentGatewayAdapterTest {

    private StoneCredentialsResolver credentialsResolver;
    private StoneWebClient stoneWebClient;
    private StonePaymentGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        credentialsResolver = mock(StoneCredentialsResolver.class);
        stoneWebClient = mock(StoneWebClient.class);
        adapter = new StonePaymentGatewayAdapter(credentialsResolver, stoneWebClient);

        when(credentialsResolver.baseUrl()).thenReturn("https://api.pagar.me");
        when(credentialsResolver.authorizationHeader()).thenReturn("Basic abc123");
        when(credentialsResolver.serviceRefererName()).thenReturn("partner-ref");
    }

    @Test
    void shouldBuildDirectStonePayloadAndReturnPendingResult() {
        PaymentCommand command = new PaymentCommand(
                dev.kalles.sale.payment.domain.PaymentProvider.STONE,
                PaymentFlow.TERMINAL,
                "ERP-SALE-1001",
                new BigDecimal("125.00"),
                "6N021234",
                "idem-1",
                "Venda PDV 1001",
                PaymentMethodType.CREDIT_CARD,
                Map.of(
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
        );

        when(stoneWebClient.exchange(eq(HttpMethod.POST), eq("https://api.pagar.me/core/v5/orders/"), any(), any()))
                .thenReturn(ResponseEntity.ok("""
                        {
                          "id": "or_stone_1001",
                          "code": "ST1001",
                          "amount": 12500,
                          "closed": false,
                          "status": "pending",
                          "poi_payment_settings": {
                            "display_name": "Pedido #1001"
                          }
                        }
                        """));

        var result = adapter.processPayment(command);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(stoneWebClient).exchange(eq(HttpMethod.POST), eq("https://api.pagar.me/core/v5/orders/"), bodyCaptor.capture(), any());

        assertThat(bodyCaptor.getValue()).contains("\"payment_setup\"");
        assertThat(bodyCaptor.getValue()).contains("\"type\":\"credit\"");
        assertThat(bodyCaptor.getValue()).contains("\"devices_serial_number\":[\"6N021234\"]");
        assertThat(bodyCaptor.getValue()).contains("\"externalReference\":\"ERP-SALE-1001\"");
        assertThat(result.providerOrderId()).isEqualTo("or_stone_1001");
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.metadata()).containsEntry("displayName", "Pedido #1001");
    }

    @Test
    void shouldRejectStonePaymentWithoutCustomerName() {
        PaymentCommand command = new PaymentCommand(
                dev.kalles.sale.payment.domain.PaymentProvider.STONE,
                PaymentFlow.TERMINAL,
                "ERP-SALE-1002",
                new BigDecimal("50.00"),
                "6N021235",
                "idem-2",
                "Venda 1002",
                PaymentMethodType.UNSPECIFIED,
                Map.of(
                        "stoneFlow", "DIRECT",
                        "itemDescription", "Venda 1002"
                )
        );

        assertThatThrownBy(() -> adapter.processPayment(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerName is required for STONE");
    }

    @Test
    void shouldMapOpenOrderLimitConflictFromStone() {
        PaymentCommand command = new PaymentCommand(
                dev.kalles.sale.payment.domain.PaymentProvider.STONE,
                PaymentFlow.TERMINAL,
                "ERP-SALE-1003",
                new BigDecimal("10.00"),
                "6N021241",
                "idem-3",
                "Venda 1003",
                PaymentMethodType.CREDIT_CARD,
                Map.of(
                        "stoneFlow", "LIST",
                        "customerName", "Bruce Banner",
                        "itemDescription", "Venda 1003"
                )
        );

        when(stoneWebClient.exchange(eq(HttpMethod.POST), eq("https://api.pagar.me/core/v5/orders/"), any(), any()))
                .thenReturn(new ResponseEntity<>("", HttpStatus.CONFLICT));

        assertThatThrownBy(() -> adapter.processPayment(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("STONE open order limit reached for terminal");
    }
}
