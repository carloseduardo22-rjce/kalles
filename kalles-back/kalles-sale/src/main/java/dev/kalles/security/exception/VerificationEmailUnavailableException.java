package dev.kalles.security.exception;

public class VerificationEmailUnavailableException extends RuntimeException {

    public static final String CODE = "VERIFICATION_EMAIL_UNAVAILABLE";

    public VerificationEmailUnavailableException() {
        super("Envio de e-mail nao configurado: nao e possivel enviar o codigo de verificacao.");
    }
}
