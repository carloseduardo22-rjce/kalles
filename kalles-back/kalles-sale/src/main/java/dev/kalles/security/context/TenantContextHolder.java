package dev.kalles.security.context;

import dev.kalles.security.exception.TenantContextRequiredException;

import java.util.UUID;

public class TenantContextHolder {

    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) {
        CONTEXT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CONTEXT.get();
    }

    public static UUID requireTenantId() {
        UUID tenantId = CONTEXT.get();
        if (tenantId == null) {
            throw new TenantContextRequiredException();
        }
        return tenantId;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}