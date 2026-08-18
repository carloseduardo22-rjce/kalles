package dev.kalles.security.context;

import dev.kalles.security.exception.CompanyContextRequiredException;

import java.util.UUID;

public class CompanyContextHolder {

    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    public static void setCompanyId(UUID companyId) {
        CONTEXT.set(companyId);
    }

    public static UUID getCompanyId() {
        return CONTEXT.get();
    }

    public static UUID requireCompanyId() {
        UUID companyId = CONTEXT.get();
        if (companyId == null) {
            throw new CompanyContextRequiredException();
        }
        return companyId;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
