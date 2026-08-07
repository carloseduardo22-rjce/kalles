package dev.kalles.core.service;

public interface Session {
    
    String getToken();

    boolean isOpen();

    default boolean allowsElectronicPayments() {
        return true;
    }
}
