package dev.kalles.security.exception;

public class TenantContextRequiredException extends RuntimeException {

    public static final String CODE = "TENANT_CONTEXT_REQUIRED";

    public TenantContextRequiredException() {
        super("Esta operacao exige um tenant autenticado no contexto.");
    }
}
