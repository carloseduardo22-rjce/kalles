package dev.kalles.payment.adapter.out.mercadopago;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MercadoPagoPaymentWebhookAdapter")
class MercadoPagoPaymentWebhookAdapterTest {

    private static final String SECRET = "segredo-do-webhook";
    private static final String REQUEST_ID = "req-9f2c1a";
    private static final String DATA_ID = "PAY-00123";
    private static final String TIMESTAMP = "1717171717";

    @Test
    @DisplayName("deve aceitar assinatura valida")
    void shouldAcceptValidSignature() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertTrue(adapter.validateSignature(signatureHeader(expectedHash()), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve aceitar assinatura valida em caixa alta")
    void shouldAcceptValidSignatureInUpperCase() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertTrue(adapter.validateSignature(
                signatureHeader(expectedHash().toUpperCase(Locale.ROOT)), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar assinatura de outro segredo")
    void shouldRejectSignatureFromAnotherSecret() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter("outro-segredo");

        assertFalse(adapter.validateSignature(signatureHeader(expectedHash()), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar assinatura que difere apenas no ultimo caractere")
    void shouldRejectSignatureDifferingOnlyInTheLastCharacter() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);
        String hash = expectedHash();
        String tampered = hash.substring(0, hash.length() - 1) + (hash.endsWith("0") ? "1" : "0");

        assertFalse(adapter.validateSignature(signatureHeader(tampered), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar quando o request id nao e o assinado")
    void shouldRejectWhenRequestIdWasNotSigned() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertFalse(adapter.validateSignature(signatureHeader(expectedHash()), "outro-request-id", DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar quando o segredo nao esta configurado")
    void shouldRejectWhenSecretIsNotConfigured() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter("");

        assertFalse(adapter.validateSignature(signatureHeader(expectedHash()), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar header sem o campo v1")
    void shouldRejectHeaderWithoutV1Field() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertFalse(adapter.validateSignature("ts=" + TIMESTAMP, REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar header sem o campo ts")
    void shouldRejectHeaderWithoutTimestampField() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertFalse(adapter.validateSignature("v1=" + expectedHash(), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar hash que nao e hexadecimal")
    void shouldRejectHashThatIsNotHexadecimal() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertFalse(adapter.validateSignature(signatureHeader("nao-e-hexadecimal"), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("deve rejeitar header nulo")
    void shouldRejectNullHeader() {
        MercadoPagoPaymentWebhookAdapter adapter = new MercadoPagoPaymentWebhookAdapter(SECRET);

        assertFalse(adapter.validateSignature(null, REQUEST_ID, DATA_ID));
    }

    private String signatureHeader(String hash) {
        return "ts=" + TIMESTAMP + ",v1=" + hash;
    }

    private String expectedHash() {
        String manifest = "id:%s;request-id:%s;ts:%s;".formatted(
                DATA_ID.toLowerCase(Locale.ROOT), REQUEST_ID, TIMESTAMP);
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
