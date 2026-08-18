package dev.kalles.security.service;

import com.github.benmanes.caffeine.cache.Ticker;
import dev.kalles.security.exception.RateLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthenticationProtectionService")
class AuthenticationProtectionServiceTest {

    private static final String EMAIL = "operador@kalles.dev";
    private static final String TENANT_ID = "123e4567-e89b-12d3-a456-426614174111";

    private final AuthenticationProtectionService service = new AuthenticationProtectionService();

    @Test
    @DisplayName("deve liberar o login enquanto as falhas nao atingem o limite")
    void shouldAllowLoginBelowTheFailureLimit() {
        for (int attempt = 0; attempt < 4; attempt++) {
            service.registerLoginFailure(EMAIL, TENANT_ID);
        }

        assertThatCode(() -> service.assertLoginAllowed(EMAIL, TENANT_ID)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve bloquear o login na quinta falha")
    void shouldBlockLoginAtTheFifthFailure() {
        for (int attempt = 0; attempt < 5; attempt++) {
            service.registerLoginFailure(EMAIL, TENANT_ID);
        }

        assertThatThrownBy(() -> service.assertLoginAllowed(EMAIL, TENANT_ID))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("deve isolar o bloqueio por tenant")
    void shouldScopeTheBlockByTenant() {
        for (int attempt = 0; attempt < 5; attempt++) {
            service.registerLoginFailure(EMAIL, TENANT_ID);
        }

        assertThatCode(() -> service.assertLoginAllowed(EMAIL, "outro-tenant")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve liberar o login apos um acesso bem sucedido")
    void shouldReleaseTheBlockAfterASuccessfulLogin() {
        for (int attempt = 0; attempt < 5; attempt++) {
            service.registerLoginFailure(EMAIL, TENANT_ID);
        }
        service.registerLoginSuccess(EMAIL, TENANT_ID);

        assertThatCode(() -> service.assertLoginAllowed(EMAIL, TENANT_ID)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve bloquear a verificacao na quinta falha")
    void shouldBlockVerificationAtTheFifthFailure() {
        for (int attempt = 0; attempt < 5; attempt++) {
            service.registerVerificationFailure(EMAIL, TENANT_ID);
        }

        assertThatThrownBy(() -> service.assertVerificationAllowed(EMAIL, TENANT_ID))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("deve bloquear o quarto reenvio na mesma janela")
    void shouldBlockTheFourthResendInTheSameWindow() {
        for (int attempt = 0; attempt < 3; attempt++) {
            service.assertResendAllowed(EMAIL, TENANT_ID);
        }

        assertThatThrownBy(() -> service.assertResendAllowed(EMAIL, TENANT_ID))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("deve parar de rastrear as chaves depois que a janela delas expira")
    void shouldStopTrackingKeysAfterTheirWindowExpires() {
        FakeTicker ticker = new FakeTicker();
        AuthenticationProtectionService trackedService = new AuthenticationProtectionService(ticker);

        for (int attempt = 0; attempt < 1000; attempt++) {
            String email = "atacante-" + attempt + "@kalles.dev";
            trackedService.registerLoginFailure(email, TENANT_ID);
            trackedService.registerVerificationFailure(email, TENANT_ID);
            trackedService.assertResendAllowed(email, TENANT_ID);
        }

        assertThat(trackedService.trackedKeys()).isEqualTo(3000);

        ticker.advance(Duration.ofMinutes(31));

        assertThat(trackedService.trackedKeys()).isZero();
    }

    @Test
    @DisplayName("deve manter o bloqueio enquanto a janela nao expira")
    void shouldKeepTheBlockWhileTheWindowHasNotExpired() {
        FakeTicker ticker = new FakeTicker();
        AuthenticationProtectionService trackedService = new AuthenticationProtectionService(ticker);

        for (int attempt = 0; attempt < 5; attempt++) {
            trackedService.registerLoginFailure(EMAIL, TENANT_ID);
        }

        ticker.advance(Duration.ofMinutes(14));

        assertThatThrownBy(() -> trackedService.assertLoginAllowed(EMAIL, TENANT_ID))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("deve tratar email nulo como anonimo, sem quebrar")
    void shouldTreatNullEmailAsAnonymous() {
        assertThatCode(() -> service.registerLoginFailure(null, null)).doesNotThrowAnyException();
        assertThatCode(() -> service.assertLoginAllowed(null, null)).doesNotThrowAnyException();
        assertThat(service).isNotNull();
    }

    private static final class FakeTicker implements Ticker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        private void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }
}
