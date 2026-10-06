package dev.kalles.note.application.service;

import dev.kalles.note.application.port.out.CryptoPort;
import dev.kalles.note.application.port.out.SensitiveContentRepositoryPort;
import dev.kalles.shared.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SensitiveContentService")
class SensitiveContentServiceTest {

    @Mock
    private CryptoPort cryptoPort;

    @Mock
    private SensitiveContentRepositoryPort repositoryPort;

    @InjectMocks
    private SensitiveContentService service;

    @Test
    @DisplayName("responde nao encontrado quando o conteudo nao existe ou e de outra conta")
    void shouldThrowNotFoundWhenContentIsMissingForAccount() {
        UUID accountId = UUID.randomUUID();
        when(repositoryPort.findByTokenAndAccountId("token", accountId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.decrypt("token", "secret", accountId));
        verify(cryptoPort, never()).decrypt(any(), any());
    }
}
