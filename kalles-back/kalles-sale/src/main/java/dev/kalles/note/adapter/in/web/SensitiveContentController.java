package dev.kalles.note.adapter.in.web;

import dev.kalles.note.adapter.in.web.dto.SensitiveContentRequest;
import dev.kalles.note.application.port.in.DecryptSensitiveContentUseCase;
import dev.kalles.note.application.port.in.EncryptSensitiveContentUseCase;
import dev.kalles.security.context.TenantContextHolder;
import dev.kalles.security.domain.Account;
import dev.kalles.security.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes/sensitive")
@RequiredArgsConstructor
public class SensitiveContentController {

    private final EncryptSensitiveContentUseCase encryptUseCase;
    private final DecryptSensitiveContentUseCase decryptUseCase;
    private final AccountRepository accountRepository;

    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encrypt(
            @RequestBody SensitiveContentRequest request,
            Authentication authentication
    ) {
        
        // Criptografa o conteúdo original e o insere no banco, obtendo e retornando só o Token Random
        UUID accountId = resolveAuthenticatedAccountId(authentication);
        String token = encryptUseCase.encryptAndSave(request.getPlainText(), request.getSecret(), accountId);
        
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/decrypt/{token}")
    public ResponseEntity<Map<String, String>> decrypt(
            @PathVariable String token,
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        UUID accountId = resolveAuthenticatedAccountId(authentication);
        String secret = payload.get("secret");
        
        // Pega do banco via Token, valida o Tenant e tenta Descriptografar
        String originalText = decryptUseCase.decrypt(token, secret, accountId);
        
        return ResponseEntity.ok(Map.of("text", originalText));
    }

    private UUID resolveAuthenticatedAccountId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Conta autenticada nao encontrada.");
        }

        Account account = accountRepository.findByTenantIdAndEmailIgnoreCase(
                        TenantContextHolder.getTenantId(),
                        authentication.getName()
                )
                .orElseThrow(() -> new IllegalArgumentException("Conta autenticada nao encontrada."));

        return account.getId();
    }
}
