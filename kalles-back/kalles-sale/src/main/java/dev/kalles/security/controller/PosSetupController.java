package dev.kalles.security.controller;

import dev.kalles.security.repository.PosDeviceSessionRepository;
import dev.kalles.security.service.GeneratePosPairingTokenUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class PosSetupController {

    private final PosDeviceSessionRepository repository;
    private final GeneratePosPairingTokenUseCase generateTokenUseCase;

    @Value("${app.security.cookies.secure:false}")
    private boolean secureCookies;

    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestBody Map<String, String> request, HttpServletResponse response) {
        String pairingToken = request.get("pairingToken");

        repository.findByTokenAndActiveTrueAndExpiresAtGreaterThan(pairingToken, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Token de pareamento inválido ou expirado."));

        // Injeta o cookie seguro HttpOnly
        Cookie cookie = new Cookie("kalles_pos_token", pairingToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 dias
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Dispositivo pareado com sucesso."));
    }

    @PostMapping("/admin/generate-token")
    public ResponseEntity<?> adminGenerateToken(@RequestBody Map<String, UUID> request) {
        UUID companyId = request.get("companyId");
        UUID posId = request.get("posId");

        if (companyId == null || posId == null) {
            return ResponseEntity.badRequest().body("Parâmetros companyId e posId são obrigatórios.");
        }

        String token = generateTokenUseCase.execute(companyId, posId);
        return ResponseEntity.ok(Map.of("pairingToken", token));
    }
}
