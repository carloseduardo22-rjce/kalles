package dev.kalles.sale.security.controller;

import dev.kalles.sale.security.dto.AuthMeResponse;
import dev.kalles.sale.security.dto.LoginRequest;
import dev.kalles.sale.security.dto.RegisterRequest;
import dev.kalles.sale.security.dto.VerifyCodeRequest;
import dev.kalles.sale.security.filter.JwtAuthenticationFilter;
import dev.kalles.sale.security.repository.AccountRepository;
import dev.kalles.sale.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return accountRepository.findByEmail(authentication.getName())
                .map(account -> ResponseEntity.ok(AuthMeResponse.from(account)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            @CookieValue(value = "kalles_pos_token", required = false) String posToken) {

        var tokens = authService.authenticate(request, posToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(tokens.refreshToken()).toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyCodeRequest request) {
        var tokens = authService.verifyCode(request);
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
    public ResponseEntity<Void> resendCode(@RequestParam String email) {
        authService.resendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);

        ResponseCookie authCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
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
                .secure(false)
                .path("/")
                .maxAge(Duration.ofHours(12))
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();
    }
}
