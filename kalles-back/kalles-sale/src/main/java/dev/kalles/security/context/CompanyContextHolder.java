package dev.kalles.security.context;

import java.util.UUID;

public class CompanyContextHolder {

    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    public static void setCompanyId(UUID companyId) {
        CONTEXT.set(companyId);
    }

    public static UUID getCompanyId() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
