package dev.kalles.sale.note.adapter.in.web;

import dev.kalles.sale.note.adapter.in.web.dto.SensitiveContentRequest;
import dev.kalles.sale.note.application.port.in.DecryptSensitiveContentUseCase;
import dev.kalles.sale.note.application.port.in.EncryptSensitiveContentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes/sensitive")
@RequiredArgsConstructor
public class SensitiveContentController {

    private final EncryptSensitiveContentUseCase encryptUseCase;
    private final DecryptSensitiveContentUseCase decryptUseCase;

    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody SensitiveContentRequest request, 
                                                     @RequestHeader("X-Tenant-ID") UUID accountId) {
        
        // Criptografa o conteúdo original e o insere no banco, obtendo e retornando só o Token Random
        String token = encryptUseCase.encryptAndSave(request.getPlainText(), request.getSecret(), accountId);
        
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/decrypt/{token}")
    public ResponseEntity<Map<String, String>> decrypt(@PathVariable String token, 
                                                     @RequestBody Map<String, String> payload, 
                                                     @RequestHeader("X-Tenant-ID") UUID accountId) {
        
        String secret = payload.get("secret");
        
        // Pega do banco via Token, valida o Tenant e tenta Descriptografar
        String originalText = decryptUseCase.decrypt(token, secret, accountId);
        
        return ResponseEntity.ok(Map.of("text", originalText));
    }
}
