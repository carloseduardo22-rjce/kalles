package dev.kalles.payment.application.port.in.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class CommandMetadata {

    private CommandMetadata() {
    }

    static Map<String, Object> immutableCopy(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }
}
