package dev.kalles.security.context;

import java.util.UUID;

public record RequestContext(UUID tenantId, UUID companyId, UUID posId) {

    private static final ScopedValue<RequestContext> CURRENT = ScopedValue.newInstance();
    private static final RequestContext EMPTY = new RequestContext(null, null, null);

    public static RequestContext empty() {
        return EMPTY;
    }

    public static RequestContext ofTenant(UUID tenantId) {
        return new RequestContext(tenantId, null, null);
    }

    public RequestContext withCompany(UUID companyId) {
        return new RequestContext(tenantId, companyId, posId);
    }

    public RequestContext withPos(UUID posId) {
        return new RequestContext(tenantId, companyId, posId);
    }

    public static RequestContext current() {
        return CURRENT.isBound() ? CURRENT.get() : EMPTY;
    }

    public static void runWithin(RequestContext context, Runnable operation) {
        ScopedValue.where(CURRENT, context).run(operation);
    }

    public static <R, X extends Throwable> R callWithin(RequestContext context,
                                                         ScopedValue.CallableOp<? extends R, X> operation) throws X {
        return ScopedValue.where(CURRENT, context).call(operation);
    }
}
