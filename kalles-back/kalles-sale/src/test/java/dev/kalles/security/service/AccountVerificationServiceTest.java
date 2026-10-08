package dev.kalles.security.service;

import dev.kalles.security.entity.Account;
import dev.kalles.security.entity.AccountVerification;
import dev.kalles.security.enums.AccountRole;
import dev.kalles.security.exception.VerificationCodeRejectedException;
import dev.kalles.security.repository.AccountVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountVerificationService")
class AccountVerificationServiceTest {

    @Mock
    private AccountVerificationRepository accountVerificationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AccountVerificationService service;

    private Account account;

    @BeforeEach
    void setUp() {
        service = new AccountVerificationService(accountVerificationRepository, eventPublisher);
        account = new Account(UUID.randomUUID(), "Conta", "conta@kalles.local", "encoded", AccountRole.ADMIN);
        account.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("deve verificar a conta com codigo valido")
    void shouldVerifyAccountWithValidCode() {
        AccountVerification verification = verification(LocalDateTime.now().plusMinutes(10), false);
        when(accountVerificationRepository.findFirstByAccountIdAndCodeOrderByCreatedAtDesc(account.getId(), "123456"))
                .thenReturn(Optional.of(verification));

        service.verifyCode(account, "123456");

        assertTrue(verification.isVerified());
        assertTrue(account.isVerified());
    }

    @Test
    @DisplayName("deve recusar codigo inexistente")
    void shouldRejectUnknownCode() {
        when(accountVerificationRepository.findFirstByAccountIdAndCodeOrderByCreatedAtDesc(account.getId(), "000000"))
                .thenReturn(Optional.empty());

        VerificationCodeRejectedException exception = assertThrows(
                VerificationCodeRejectedException.class,
                () -> service.verifyCode(account, "000000")
        );

        assertEquals("VERIFICATION_CODE_INVALID", exception.getCode());
        assertFalse(account.isVerified());
    }

    @Test
    @DisplayName("deve recusar codigo ja utilizado")
    void shouldRejectAlreadyUsedCode() {
        when(accountVerificationRepository.findFirstByAccountIdAndCodeOrderByCreatedAtDesc(account.getId(), "123456"))
                .thenReturn(Optional.of(verification(LocalDateTime.now().plusMinutes(10), true)));

        VerificationCodeRejectedException exception = assertThrows(
                VerificationCodeRejectedException.class,
                () -> service.verifyCode(account, "123456")
        );

        assertEquals("VERIFICATION_CODE_ALREADY_USED", exception.getCode());
        assertFalse(account.isVerified());
    }

    @Test
    @DisplayName("deve recusar codigo expirado")
    void shouldRejectExpiredCode() {
        when(accountVerificationRepository.findFirstByAccountIdAndCodeOrderByCreatedAtDesc(account.getId(), "123456"))
                .thenReturn(Optional.of(verification(LocalDateTime.now().minusMinutes(1), false)));

        VerificationCodeRejectedException exception = assertThrows(
                VerificationCodeRejectedException.class,
                () -> service.verifyCode(account, "123456")
        );

        assertEquals("VERIFICATION_CODE_EXPIRED", exception.getCode());
        assertFalse(account.isVerified());
    }

    private AccountVerification verification(LocalDateTime expiresAt, boolean verified) {
        AccountVerification verification = new AccountVerification(account.getId(), "123456", expiresAt);
        verification.setVerified(verified);
        return verification;
    }
}
