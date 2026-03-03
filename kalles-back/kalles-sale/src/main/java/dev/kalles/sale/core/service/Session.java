package dev.kalles.sale.core.service;

public interface Session {
    
    String getToken();

    boolean isOpen();
}
