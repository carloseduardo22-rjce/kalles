package dev.kalles.security.exception;

public class CompanyContextRequiredException extends RuntimeException {

    public static final String CODE = "COMPANY_CONTEXT_REQUIRED";

    public CompanyContextRequiredException() {
        super("Esta rota exige uma filial ativa. Envie X-Company-ID com uma filial acessivel para o tenant autenticado.");
    }
}
