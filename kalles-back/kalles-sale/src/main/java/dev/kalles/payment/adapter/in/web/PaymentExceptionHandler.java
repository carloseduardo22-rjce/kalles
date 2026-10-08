package dev.kalles.payment.adapter.in.web;

import dev.kalles.payment.exception.PaymentProviderIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(0)
@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentProviderIntegrationException.class)
    public ProblemDetail handleProviderIntegration(PaymentProviderIntegrationException ex) {
        log.warn("Falha de integracao com o provedor de pagamento", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY,
            "Falha na comunicação com o provedor de pagamento."
        );
        problem.setTitle("Falha de integração com provedor de pagamento");
        problem.setProperty("code", "PAYMENT_PROVIDER_INTEGRATION_FAILED");
        return problem;
    }
}
