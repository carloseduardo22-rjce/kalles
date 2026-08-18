package dev.kalles.fiscal.adapter.in.web;

import dev.kalles.fiscal.adapter.in.web.dto.FiscalDocumentResponse;
import dev.kalles.fiscal.exception.FiscalConflictException;
import dev.kalles.fiscal.exception.FiscalIntegrationException;
import dev.kalles.fiscal.exception.FiscalRejectionException;
import dev.kalles.fiscal.exception.FiscalValidationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class FiscalExceptionHandler {

    @ExceptionHandler(FiscalValidationException.class)
    public ProblemDetail handleFiscalValidation(FiscalValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle("Documento fiscal invalido");
        return problem;
    }

    @ExceptionHandler(FiscalConflictException.class)
    public ProblemDetail handleFiscalConflict(FiscalConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problem.setTitle("Emissao fiscal indisponivel");
        return problem;
    }

    @ExceptionHandler(FiscalRejectionException.class)
    public ResponseEntity<FiscalDocumentResponse> handleFiscalRejection(FiscalRejectionException ex) {
        return ResponseEntity
                .unprocessableEntity()
                .body(FiscalDocumentResponse.from(ex.document()));
    }

    @ExceptionHandler(FiscalIntegrationException.class)
    public ProblemDetail handleFiscalIntegration(FiscalIntegrationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY,
            ex.getMessage()
        );
        problem.setTitle("Falha de integracao fiscal");
        return problem;
    }
}
