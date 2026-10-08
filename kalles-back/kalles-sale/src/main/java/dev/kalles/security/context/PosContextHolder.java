package dev.kalles.security.context;

import java.util.UUID;

public class PosContextHolder {

    public static UUID getPosId() {
        return RequestContext.current().posId();
    }
}
