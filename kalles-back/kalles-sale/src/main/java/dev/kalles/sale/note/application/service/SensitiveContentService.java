package dev.kalles.sale.note.application.service;

import dev.kalles.sale.security.domain.Account;
import dev.kalles.sale.note.application.port.in.DecryptSensitiveContentUseCase;
import dev.kalles.sale.note.application.port.in.EncryptSensitiveContentUseCase;
import dev.kalles.sale.note.application.port.out.CryptoPort;
import dev.kalles.sale.note.application.port.out.SensitiveContentRepositoryPort;
import dev.kalles.sale.note.domain.SensitiveContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensitiveContentService implements EncryptSensitiveContentUseCase, DecryptSensitiveContentUseCase {

    private final CryptoPort cryptoPort;
    private final SensitiveContentRepositoryPort repositoryPort;

    @Override
    @Transactional
    public String encryptAndSave(String plainText, String secret, UUID accountId) {
        String encryptedText = cryptoPort.encrypt(plainText, secret);
        
        String randomToken = UUID.randomUUID().toString().replace("-", "");

        Account dummyAccountRef = new Account();
        dummyAccountRef.setId(accountId);

        SensitiveContent sensitiveContent = new SensitiveContent();
        sensitiveContent.setToken(randomToken);
        sensitiveContent.setEncryptedText(encryptedText);
        sensitiveContent.setAccount(dummyAccountRef);

        repositoryPort.save(sensitiveContent);

        return randomToken;
    }

    @Override
    @Transactional(readOnly = true)
    public String decrypt(String token, String secret, UUID accountId) {
        SensitiveContent sensitiveContent = repositoryPort.findByTokenAndAccountId(token, accountId)
                .orElseThrow(() -> new RuntimeException("Content not found or unauthorized"));

        return cryptoPort.decrypt(sensitiveContent.getEncryptedText(), secret);
    }
}
