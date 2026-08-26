package dev.kalles.shared.service;

import java.util.UUID;

public interface Session {

    UUID getId();

    boolean isOpen();

    default boolean allowsElectronicPayments() {
        return true;
    }
}
