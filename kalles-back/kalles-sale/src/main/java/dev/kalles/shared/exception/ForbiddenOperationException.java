package dev.kalles.shared.exception;

public class ForbiddenOperationException extends RuntimeException {

    private final String code;

    public ForbiddenOperationException(String message) {
        this(null, message);
    }

    public ForbiddenOperationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
