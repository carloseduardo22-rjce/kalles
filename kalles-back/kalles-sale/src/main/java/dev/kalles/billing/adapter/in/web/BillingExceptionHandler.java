package dev.kalles.billing.adapter.in.web;

import dev.kalles.billing.exception.BillingIntegrationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class BillingExceptionHandler {

    @ExceptionHandler(BillingIntegrationException.class)
    public ProblemDetail handleBillingIntegration(BillingIntegrationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY,
            ex.getMessage()
        );
        problem.setTitle("Falha de integração com gateway de pagamento");
        return problem;
    }
}
