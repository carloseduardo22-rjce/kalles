package dev.kalles.sale.security.context;

import java.util.UUID;

public class PosContextHolder {

    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    public static void setPosId(UUID posId) {
        CONTEXT.set(posId);
    }

    public static UUID getPosId() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
