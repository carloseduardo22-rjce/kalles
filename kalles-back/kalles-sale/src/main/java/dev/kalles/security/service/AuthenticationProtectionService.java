package dev.kalles.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import dev.kalles.security.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthenticationProtectionService {

    private static final AttemptPolicy LOGIN_POLICY = new AttemptPolicy(5, Duration.ofMinutes(15), Duration.ofMinutes(15));
    private static final AttemptPolicy VERIFY_POLICY = new AttemptPolicy(5, Duration.ofMinutes(15), Duration.ofMinutes(15));
    private static final RatePolicy RESEND_POLICY = new RatePolicy(3, Duration.ofMinutes(15));
    private static final long MAX_TRACKED_KEYS = 100_000;

    private final Cache<String, FailureState> loginFailures;
    private final Cache<String, FailureState> verificationFailures;
    private final Cache<String, RateState> resendAttempts;

    public AuthenticationProtectionService() {
        this(Ticker.systemTicker());
    }

    AuthenticationProtectionService(Ticker ticker) {
        this.loginFailures = buildCache(ticker, LOGIN_POLICY.window().plus(LOGIN_POLICY.lockDuration()));
        this.verificationFailures = buildCache(ticker, VERIFY_POLICY.window().plus(VERIFY_POLICY.lockDuration()));
        this.resendAttempts = buildCache(ticker, RESEND_POLICY.window());
    }

    private static <T> Cache<String, T> buildCache(Ticker ticker, Duration retention) {
        return Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfterWrite(retention)
                .maximumSize(MAX_TRACKED_KEYS)
                .build();
    }

    public void assertLoginAllowed(String email, String tenantId) {
        assertFailurePolicyAllowed(loginFailures.asMap(), buildScopedKey(email, tenantId), LOGIN_POLICY,
                "Muitas tentativas de login. Aguarde alguns minutos antes de tentar novamente.");
    }

    public void registerLoginFailure(String email, String tenantId) {
        registerFailure(loginFailures.asMap(), buildScopedKey(email, tenantId), LOGIN_POLICY);
    }

    public void registerLoginSuccess(String email, String tenantId) {
        loginFailures.invalidate(buildScopedKey(email, tenantId));
    }

    public void assertVerificationAllowed(String email, String tenantId) {
        assertFailurePolicyAllowed(verificationFailures.asMap(), buildScopedKey(email, tenantId), VERIFY_POLICY,
                "Muitas tentativas de verificacao. Aguarde alguns minutos antes de tentar novamente.");
    }

    public void registerVerificationFailure(String email, String tenantId) {
        registerFailure(verificationFailures.asMap(), buildScopedKey(email, tenantId), VERIFY_POLICY);
    }

    public void registerVerificationSuccess(String email, String tenantId) {
        verificationFailures.invalidate(buildScopedKey(email, tenantId));
    }

    public void assertResendAllowed(String email, String tenantId) {
        Instant now = Instant.now();
        String key = buildScopedKey(email, tenantId);
        resendAttempts.asMap().compute(key, (ignored, existing) -> {
            RateState state = existing;
            if (state == null || now.isAfter(state.windowStartedAt.plus(RESEND_POLICY.window()))) {
                state = new RateState(now, 0);
            }

            if (state.attempts >= RESEND_POLICY.maxAttempts()) {
                throw new RateLimitExceededException("Limite de reenvio atingido. Aguarde alguns minutos antes de solicitar um novo codigo.");
            }

            state.attempts++;
            return state;
        });
    }

    private void assertFailurePolicyAllowed(
            Map<String, FailureState> states,
            String key,
            AttemptPolicy policy,
            String message
    ) {
        FailureState state = states.get(key);
        if (state == null) {
            return;
        }

        Instant now = Instant.now();
        synchronized (state) {
            if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
                throw new RateLimitExceededException(message);
            }
            if (now.isAfter(state.windowStartedAt.plus(policy.window()))) {
                states.remove(key);
            }
        }
    }

    private void registerFailure(Map<String, FailureState> states, String key, AttemptPolicy policy) {
        Instant now = Instant.now();
        states.compute(key, (ignored, existing) -> {
            FailureState state = existing;
            if (state == null || now.isAfter(state.windowStartedAt.plus(policy.window()))) {
                state = new FailureState(now);
            }

            synchronized (state) {
                state.failures++;
                if (state.failures >= policy.maxFailures()) {
                    state.lockedUntil = now.plus(policy.lockDuration());
                }
            }
            return state;
        });
    }

    long trackedKeys() {
        loginFailures.cleanUp();
        verificationFailures.cleanUp();
        resendAttempts.cleanUp();
        return loginFailures.estimatedSize() + verificationFailures.estimatedSize() + resendAttempts.estimatedSize();
    }

    private String buildScopedKey(String email, String tenantId) {
        return normalize(email) + "|" + normalize(tenantId);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record AttemptPolicy(int maxFailures, Duration window, Duration lockDuration) {
    }

    private record RatePolicy(int maxAttempts, Duration window) {
    }

    private static final class FailureState {
        private final Instant windowStartedAt;
        private int failures;
        private Instant lockedUntil;

        private FailureState(Instant windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }
    }

    private static final class RateState {
        private final Instant windowStartedAt;
        private int attempts;

        private RateState(Instant windowStartedAt, int attempts) {
            this.windowStartedAt = windowStartedAt;
            this.attempts = attempts;
        }
    }
}
