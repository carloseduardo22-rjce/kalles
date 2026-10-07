package dev.kalles.billing.adapter.out.stripe;

import dev.kalles.billing.exception.BillingWebhookRejectedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DefaultStripeSdkClient")
class DefaultStripeSdkClientTest {

    private final DefaultStripeSdkClient client = new DefaultStripeSdkClient("sk_test_unit");

    @Test
    @DisplayName("rejeita webhook com assinatura invalida como requisicao ruim")
    void shouldRejectWebhookWithInvalidSignature() {
        assertThrows(BillingWebhookRejectedException.class, () ->
                client.parseWebhook("{\"id\":\"evt_1\"}", "t=1,v1=invalid", "whsec_unit"));
    }
}
