package dev.kalles.sale.mercadopago.application.service;

import dev.kalles.sale.note.application.port.out.CryptoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Base64;

@Service
public class TenantCredentialCipherService {

    private final CryptoPort cryptoPort;
    private final String secret;

    public TenantCredentialCipherService(
            CryptoPort cryptoPort,
            @Value("${security.encryption.tenant-credentials-secret}") String secret) {
        this.cryptoPort = cryptoPort;
        this.secret = secret;
    }

    public String encrypt(String value) {
        if (!StringUtils.hasText(value) || isEncrypted(value)) {
            return value;
        }
        return cryptoPort.encrypt(value, secret);
    }

    public String decrypt(String value) {
        if (!StringUtils.hasText(value) || !isEncrypted(value)) {
            return value;
        }
        return cryptoPort.decrypt(value, secret);
    }

    public boolean isEncrypted(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String[] parts = value.split(":");
        if (parts.length != 3) {
            return false;
        }

        try {
            Base64.Decoder decoder = Base64.getDecoder();
            decoder.decode(parts[0]);
            decoder.decode(parts[1]);
            decoder.decode(parts[2]);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
