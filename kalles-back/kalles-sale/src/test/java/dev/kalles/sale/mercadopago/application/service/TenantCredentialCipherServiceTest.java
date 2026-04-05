package dev.kalles.sale.mercadopago.application.service;

import dev.kalles.sale.note.adapter.out.crypto.AesCryptoAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TenantCredentialCipherService")
class TenantCredentialCipherServiceTest {

    private final TenantCredentialCipherService service =
            new TenantCredentialCipherService(new AesCryptoAdapter(), "test-secret");

    @Test
    @DisplayName("Encrypts and decrypts tenant credentials transparently")
    void encryptsAndDecryptsTenantCredentials() {
        String plainText = "APP_USR-sensitive-token";

        String encrypted = service.encrypt(plainText);

        assertNotEquals(plainText, encrypted);
        assertTrue(service.isEncrypted(encrypted));
        assertEquals(plainText, service.decrypt(encrypted));
    }

    @Test
    @DisplayName("Keeps legacy plain text readable during transition")
    void keepsLegacyPlainTextReadableDuringTransition() {
        String legacyValue = "3268408672";

        assertFalse(service.isEncrypted(legacyValue));
        assertEquals(legacyValue, service.decrypt(legacyValue));
    }
}
