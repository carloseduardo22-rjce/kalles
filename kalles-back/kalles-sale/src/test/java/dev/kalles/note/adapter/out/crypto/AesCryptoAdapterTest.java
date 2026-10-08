package dev.kalles.note.adapter.out.crypto;

import dev.kalles.note.exception.SensitiveContentSecretInvalidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AesCryptoAdapter")
class AesCryptoAdapterTest {

    private final AesCryptoAdapter adapter = new AesCryptoAdapter();

    @Test
    @DisplayName("devolve o texto original com o segredo correto")
    void shouldDecryptWithTheSameSecret() {
        String encrypted = adapter.encrypt("conteudo", "segredo");

        assertEquals("conteudo", adapter.decrypt(encrypted, "segredo"));
    }

    @Test
    @DisplayName("rejeita o segredo errado como regra de negocio")
    void shouldRejectWrongSecret() {
        String encrypted = adapter.encrypt("conteudo", "segredo");

        assertThrows(SensitiveContentSecretInvalidException.class, () -> adapter.decrypt(encrypted, "outro"));
    }
}
