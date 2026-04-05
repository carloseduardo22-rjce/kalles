package dev.kalles.sale.note.application.port.in;

import java.util.UUID;

public interface EncryptSensitiveContentUseCase {
    String encryptAndSave(String plainText, String secret, UUID accountId);
}
