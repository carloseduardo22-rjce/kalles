package dev.kalles.cashregister.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class CashRegisterExceptionHandler {

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
}
