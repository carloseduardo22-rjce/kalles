package dev.kalles.note.application.port.in;

import java.util.UUID;

public interface DecryptSensitiveContentUseCase {
    String decrypt(String token, String secret, UUID accountId);
}
