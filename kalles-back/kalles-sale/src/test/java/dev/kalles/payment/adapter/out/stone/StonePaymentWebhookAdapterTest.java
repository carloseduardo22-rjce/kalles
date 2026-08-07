package dev.kalles.payment.adapter.out.stone;

import dev.kalles.payment.domain.PaymentMethodType;
import dev.kalles.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StonePaymentWebhookAdapterTest {

    private final StonePaymentWebhookAdapter adapter = new StonePaymentWebhookAdapter("test-secret");

    @Test
    void shouldTranslateChargePaidWebhookIntoApprovedEvent() {
        var event = adapter.parseEvent(Map.of(
                "type", "charge.paid",
                "data", Map.of(
                        "id", "ch_stone_987",
                        "paid_amount", 12500,
                        "status", "paid",
                        "payment_method", "credit",
                        "order", Map.of(
                                "id", "or_stone_123",
                                "amount", 12500,
                                "metadata", Map.of("externalReference", "ERP-SALE-1001")
                        ),
                        "metadata", Map.of(
                                "account_funding_source", "Credit",
                                "terminal_serial_number", "6N021234"
                        )
                )
        ));

        assertThat(event).isNotNull();
        assertThat(event.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(event.methodType()).isEqualTo(PaymentMethodType.CREDIT_CARD);
        assertThat(event.providerOrderId()).isEqualTo("or_stone_123");
        assertThat(event.providerPaymentId()).isEqualTo("ch_stone_987");
        assertThat(event.metadata()).containsEntry("terminalSerialNumber", "6N021234");
    }

    @Test
    void shouldRegisterAmountDivergenceForListedOrderWebhook() {
        var event = adapter.parseEvent(Map.of(
                "type", "charge.paid",
                "data", Map.of(
                        "id", "ch_stone_list_1",
                        "paid_amount", 10000,
                        "status", "paid",
                        "order", Map.of(
                                "id", "or_stone_list_1",
                                "amount", 8990,
                                "metadata", Map.of("externalReference", "ERP-LIST-1")
                        ),
                        "metadata", Map.of("terminal_serial_number", "6N021236")
                )
        ));

        assertThat(event).isNotNull();
        assertThat(event.metadata()).containsEntry("amountDivergence", true);
        assertThat(event.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldIgnoreUnsupportedWebhookType() {
        assertThat(adapter.parseEvent(Map.of("type", "charge.unknown", "data", Map.of()))).isNull();
    }
}
