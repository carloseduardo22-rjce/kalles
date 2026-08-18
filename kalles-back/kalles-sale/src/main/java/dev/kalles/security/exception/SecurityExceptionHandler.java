package dev.kalles.security.exception;

import dev.kalles.security.service.InvalidRefreshTokenException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage()
        );
        problem.setTitle("Sessão expirada");
        return problem;
    }

    @ExceptionHandler(CompanyContextRequiredException.class)
    public ProblemDetail handleCompanyContextRequired(CompanyContextRequiredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle("Contexto de filial obrigatorio");
        problem.setProperty("code", CompanyContextRequiredException.CODE);
        return problem;
    }

    @ExceptionHandler(TenantContextRequiredException.class)
    public ProblemDetail handleTenantContextRequired(TenantContextRequiredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problem.setTitle("Contexto de tenant obrigatorio");
        problem.setProperty("code", TenantContextRequiredException.CODE);
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
