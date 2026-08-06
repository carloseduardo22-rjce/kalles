package dev.kalles.sale.exception;

import dev.kalles.sale.billing.exception.BillingIntegrationException;
import dev.kalles.sale.cashregister.exception.ActiveSessionAlreadyExistsException;
import dev.kalles.sale.cashregister.exception.CashRegisterNotFoundException;
import dev.kalles.sale.cashregister.exception.OperatorAlreadyInSessionException;
import dev.kalles.sale.cashregister.exception.OperatorNotFoundException;
import dev.kalles.sale.core.exception.ForbiddenOperationException;
import dev.kalles.sale.core.exception.GoalDomainException;
import dev.kalles.sale.core.exception.InsufficientStockException;
import dev.kalles.sale.core.exception.NotFoundException;
import dev.kalles.sale.fiscal.adapter.in.web.dto.FiscalDocumentResponse;
import dev.kalles.sale.fiscal.exception.FiscalConflictException;
import dev.kalles.sale.fiscal.exception.FiscalIntegrationException;
import dev.kalles.sale.fiscal.exception.FiscalRejectionException;
import dev.kalles.sale.fiscal.exception.FiscalValidationException;
import dev.kalles.sale.security.exception.RateLimitExceededException;
import dev.kalles.sale.security.service.InvalidRefreshTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.OptimisticLockException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServletRequestBindingException.class)
    public ProblemDetail handleRequestBinding(ServletRequestBindingException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle("Requisição malformada");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void rethrowToSecurityFilterChain(AccessDeniedException ex) {
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return errorResponse.getBody();
        }

        log.error("Erro nao tratado ao processar requisicao", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno ao processar a requisição."
        );
        problem.setTitle("Erro interno");
        return problem;
    }

    @ExceptionHandler(GoalDomainException.class)
    public ProblemDetail handleGoalDomain(GoalDomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getMessage()
        );
        problem.setTitle("Regra de negócio violada");
        return problem;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problem.setTitle("Recurso não encontrado");
        return problem;
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ProblemDetail handleForbidden(ForbiddenOperationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.getMessage()
        );
        problem.setTitle("Operação não permitida");
        return problem;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problem.setTitle("Estoque insuficiente");
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problem.setTitle("Operação inválida para o estado atual");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle("Argumento inválido");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String detail = cause instanceof IllegalArgumentException
                ? cause.getMessage()
                : "Corpo da requisicao invalido ou malformado.";

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            detail
        );
        problem.setTitle("Requisição inválida");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            errors
        );
        problem.setTitle("Erro de validação");
        return problem;
    }

    @ExceptionHandler(ActiveSessionAlreadyExistsException.class)
    public ProblemDetail handleActiveSession(ActiveSessionAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problem.setTitle("Sessão ativa já existe");
        return problem;
    }

    @ExceptionHandler(OperatorAlreadyInSessionException.class)
    public ProblemDetail handleOperatorAlreadyInSession(OperatorAlreadyInSessionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problem.setTitle("Operador já está em sessão ativa");
        return problem;
    }

    @ExceptionHandler({CashRegisterNotFoundException.class, OperatorNotFoundException.class})
    public ProblemDetail handleCashRegisterNotFound(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problem.setTitle("Recurso não encontrado");
        return problem;
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "A venda foi modificada por outra operação. Tente novamente."
        );
        problem.setTitle("Conflito de concorrência");
        return problem;
    }
    @ExceptionHandler(BillingIntegrationException.class)
    public ProblemDetail handleBillingIntegration(BillingIntegrationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY,
            ex.getMessage()
        );
        problem.setTitle("Falha de integraÃ§Ã£o com gateway de pagamento");
        return problem;
    }

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
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage()
        );
        problem.setTitle("Sessão expirada");
        return problem;
    }
    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceeded(RateLimitExceededException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            ex.getMessage()
        );
        problem.setTitle("Muitas tentativas");
        return problem;
    }
}
