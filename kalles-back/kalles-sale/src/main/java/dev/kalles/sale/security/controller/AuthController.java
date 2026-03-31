package dev.kalles.sale.security.controller;

import dev.kalles.sale.security.dto.LoginRequest;
import dev.kalles.sale.security.dto.RegisterRequest;
import dev.kalles.sale.security.dto.VerifyCodeRequest;
import dev.kalles.sale.security.filter.JwtAuthenticationFilter;
import dev.kalles.sale.security.service.AuthService;
import dev.kalles.sale.security.context.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<java.util.Map<String, String>> me() {
        java.util.UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(java.util.Map.of("tenantId", tenantId.toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            @CookieValue(value = "kalles_pos_token", required = false) String posToken) {
        
        String token = authService.authenticate(request, posToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(token).toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        // The token is not sent here. User needs to verify through email.
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyCodeRequest request) {
        String token = authService.verifyCode(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAuthCookie(token).toString())
                .build();
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendCode(@RequestParam String email) {
        authService.resendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false) // Mude para true em prod usando HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // Mude para true em produção (assumindo que no local roda por http)
                .path("/")
                .maxAge(Duration.ofHours(12))
                .sameSite("Lax")
                .build();
    }
}
