package dev.kalles.security.context;

import dev.kalles.security.exception.TenantContextRequiredException;

import java.util.UUID;

public class TenantContextHolder {

    public static UUID getTenantId() {
        return RequestContext.current().tenantId();
    }

    public static UUID requireTenantId() {
        UUID tenantId = getTenantId();
        if (tenantId == null) {
            throw new TenantContextRequiredException();
        }
        return tenantId;
    }
}
