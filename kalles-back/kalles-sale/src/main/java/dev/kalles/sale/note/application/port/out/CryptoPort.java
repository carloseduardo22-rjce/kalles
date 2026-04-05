package dev.kalles.sale.note.application.port.out;

public interface CryptoPort {
    String encrypt(String plainText, String secret);
    String decrypt(String encryptedText, String secret);
}
