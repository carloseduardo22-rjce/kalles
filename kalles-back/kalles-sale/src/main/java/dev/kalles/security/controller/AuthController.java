package dev.kalles.security.controller;

import dev.kalles.security.dto.AuthMeResponse;
import dev.kalles.security.dto.LoginRequest;
import dev.kalles.security.dto.RegisterResponse;
import dev.kalles.security.dto.RegisterRequest;
import dev.kalles.security.dto.VerifyCodeRequest;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.filter.JwtAuthenticationFilter;
import dev.kalles.security.repository.AccountRepository;
import dev.kalles.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "kalles_refresh_token";

    private final AuthService authService;
    private final AccountRepository accountRepository;

    @Value("${app.security.cookies.secure:false}")
    private boolean secureCookies;

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return accountRepository.findByTenantIdAndEmailIgnoreCase(TenantContextHolder.getTenantId(), authentication.getName())
                .map(account -> ResponseEntity.ok(AuthMeResponse.from(account)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            @CookieValue(value = "kalles_pos_token", required = false) String posToken,
            HttpServletRequest httpServletRequest) {

        var tokens = authService.authenticate(request, posToken, resolveClientFingerprint(httpServletRequest));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(tokens.refreshToken()).toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyCodeRequest request, HttpServletRequest httpServletRequest) {
        var tokens = authService.verifyCode(request, resolveClientFingerprint(httpServletRequest));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(tokens.refreshToken()).toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        var tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(tokens.refreshToken()).toString())
                .build();
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendCode(
            @RequestParam String email,
            @RequestParam(required = false) String tenantId,
            HttpServletRequest httpServletRequest) {
        authService.resendVerificationCode(email, tenantId, resolveClientFingerprint(httpServletRequest));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/csrf")
    public ResponseEntity<java.util.Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(java.util.Map.of("token", csrfToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);

        ResponseCookie authCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    private ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(Duration.ofHours(12))
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();
    }

    private String resolveClientFingerprint(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ip = (forwardedFor != null && !forwardedFor.isBlank())
                ? forwardedFor.split(",")[0].trim()
                : request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return ip + "|" + (userAgent == null ? "unknown-agent" : userAgent);
    }
}
