package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.adapter.out.mercadopago.MercadoPagoAdapterException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentExceptionHandlerTest {

    private final PaymentExceptionHandler handler = new PaymentExceptionHandler();

    @Test
    void shouldMapProviderFailureToBadGatewayWithoutExposingTheProviderResponse() {
        ProblemDetail problem = handler.handleProviderIntegration(new MercadoPagoAdapterException(
                "Fail to create MP Order. HTTP Status: 400 - {\"error\":\"invalid_token\"}"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(problem.getProperties()).containsEntry("code", "PAYMENT_PROVIDER_INTEGRATION_FAILED");
        assertThat(problem.getDetail())
                .isEqualTo("Falha na comunicação com o provedor de pagamento.")
                .doesNotContain("invalid_token");
    }
}
